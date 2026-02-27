# Cleanup & Session Limit Documentation

## 5.1 Cleanup Job (Bắt buộc)

### Mô tả
Scheduled job tự động xóa các refresh tokens đã hết hạn khỏi database để giữ DB ổn định.

### SQL Queries
```sql
-- Delete expired tokens
DELETE FROM refresh_tokens WHERE expires_at < NOW();

-- Delete revoked tokens
DELETE FROM refresh_tokens WHERE revoked_at IS NOT NULL;
```

**Lưu ý**: Cleanup job xóa cả expired tokens VÀ revoked tokens để giữ database sạch.

### Cấu hình

**File**: `src/main/resources/application.yml`

```yaml
auth:
  # Option 1: Fixed delay (milliseconds)
  token-cleanup-interval: 3600000  # 1 hour = 3600000ms
  token-cleanup-initial-delay: 60000  # 1 minute delay before first run
  
  # Option 2: Cron expression (uncomment in RefreshTokenCleanupJob)
  # token-cleanup-cron: "0 0 * * * ?"  # Every hour at minute 0
```

### Schedule Options

| Schedule | Config Value | Description |
|----------|-------------|-------------|
| Mỗi giờ | `token-cleanup-interval: 3600000` | Default, chạy mỗi 1 giờ |
| Mỗi ngày | `token-cleanup-cron: "0 0 0 * * ?"` | Chạy lúc 00:00 mỗi ngày |
| Mỗi 6 giờ | `token-cleanup-interval: 21600000` | Chạy mỗi 6 giờ |

### Implementation

**File**: `src/main/java/com/novacore/auth/job/RefreshTokenCleanupJob.java`

- Chạy tự động với `@Scheduled`
- Log số lượng tokens đã xóa
- Error handling để đảm bảo job không crash

### Logs Example
```
INFO  - Cleanup completed: 10 expired tokens and 5 revoked tokens deleted (total: 15)
INFO  - Refresh token cleanup completed: 15 tokens deleted (expired + revoked)
DEBUG - No tokens to clean up (expired: 0, revoked: 0)
```

---

## 5.2 Session Limit (Optional)

### Mô tả
Giới hạn số lượng session đồng thời cho mỗi user. Ví dụ: tối đa 5 sessions/user.

### Cấu hình

**File**: `src/main/resources/application.yml`

```yaml
auth:
  max-sessions-per-user: 5  # Optional: Maximum concurrent sessions per user
```

**Nếu không config**: Không có giới hạn (unlimited sessions)

### Logic Flow

Khi user **login** hoặc **register**:

1. **Count active sessions**:
   ```sql
   SELECT COUNT(*) FROM refresh_tokens 
   WHERE user_id = :userId AND revoked_at IS NULL
   ```

2. **Check limit**:
   - Nếu `activeCount >= maxSessions` → Cần revoke session cũ nhất
   - Nếu `activeCount < maxSessions` → OK, tạo session mới

3. **Revoke oldest sessions** (nếu cần):
   ```sql
   SELECT * FROM refresh_tokens 
   WHERE user_id = :userId 
   ORDER BY created_at ASC
   ```
   - Revoke các session cũ nhất cho đến khi `activeCount < maxSessions`
   - Đảm bảo có chỗ cho session mới

4. **Create new session**: Tạo refresh token mới

### Example Scenario

**Config**: `max-sessions-per-user: 5`

**User đã có 5 active sessions**:
- Session 1 (created: 2024-01-01 10:00) ← Oldest
- Session 2 (created: 2024-01-01 11:00)
- Session 3 (created: 2024-01-01 12:00)
- Session 4 (created: 2024-01-01 13:00)
- Session 5 (created: 2024-01-01 14:00) ← Newest

**User login lần 6**:
1. Count: 5 active sessions
2. Check: 5 >= 5 → Cần revoke
3. Revoke: Session 1 (oldest) bị revoke
4. Create: Session 6 mới được tạo

**Kết quả**: User có 5 sessions (Session 2-6, Session 1 đã bị revoke)

### Implementation

**File**: `src/main/java/com/novacore/auth/service/impl/RefreshTokenServiceImpl.java`

Method: `revokeOldestSessionIfExceedsLimit(Long userId, int maxSessions)`

- Được gọi tự động trong `createRefreshToken()` trước khi tạo token mới
- Chỉ revoke các session **active** (chưa bị revoked)
- Revoke theo thứ tự **oldest first** (created_at ASC)

### Logs Example
```
INFO  - Revoked 1 oldest session(s) for user ID: 123 to enforce limit of 5 (had 5 active sessions)
DEBUG - Revoked oldest session (tokenId=456, createdAt=2024-01-01T10:00:00) for user ID: 123
```

---

## Testing

### Test Cleanup Job

1. **Tạo expired tokens** (set expires_at trong quá khứ)
2. **Chờ job chạy** hoặc trigger manually
3. **Kiểm tra logs** để xem số tokens đã xóa

### Test Session Limit

1. **Config**: `auth.max-sessions-per-user: 3`
2. **Login 4 lần** với cùng 1 user
3. **Kiểm tra**: Session đầu tiên (oldest) sẽ bị revoke
4. **Verify**: User chỉ có 3 active sessions

### Manual Test Commands

```bash
# Login lần 1
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "password": "pass123"}'

# Login lần 2 (với device khác)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "password": "pass123", "deviceId": "device2"}'

# ... tiếp tục login đến khi đạt limit
```

---

## Best Practices

1. **Cleanup Schedule**:
   - Production: Chạy mỗi ngày lúc off-peak hours (ví dụ: 2:00 AM)
   - Development: Chạy mỗi giờ để test nhanh hơn

2. **Session Limit**:
   - Web app: 3-5 sessions/user
   - Mobile app: 5-10 sessions/user
   - Admin users: Có thể không giới hạn

3. **Monitoring**:
   - Monitor số lượng tokens được cleanup
   - Alert nếu cleanup job fail
   - Track session limit violations

4. **Performance**:
   - Cleanup job sử dụng index trên `expires_at`
   - Session limit query sử dụng index trên `user_id` và `created_at`

