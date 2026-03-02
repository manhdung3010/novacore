# Thiết kế: Join server, Mời user, Server settings

## 1. Tổng quan

- **Join server**: User tự join (qua link/explore) → tùy setting: join trực tiếp hoặc gửi yêu cầu duyệt.
- **Mời user**: Owner/member tạo invite (link hoặc mời theo user) → user nhận và accept → join (hoặc vào hàng chờ duyệt).
- **Server settings**: Cấu hình như yêu cầu duyệt, tin nhắn chào mừng, kênh chào mừng, ai được mời.

---

## 2. Giả định

- Bảng `server_invites` (code, server_id, created_by, uses, max_uses, expire_at, temporary) đã có.
- Bảng `channels`, `messages` đã có (để gửi tin nhắn chào mừng vào kênh).
- Phân quyền chi tiết (role-based) có thể làm sau; tạm: owner có toàn quyền, member có thể mời nếu setting cho phép.

---

## 3. Data model

### 3.1 Server settings (mới)

Lưu cấu hình từng server. Có thể 1-1 với `servers` (bảng riêng hoặc cột trên `servers`).

**Bảng `server_settings`** (hoặc cột thêm vào `servers`):

| Cột | Kiểu | Mô tả |
|-----|------|--------|
| server_id | BIGINT PK, FK servers | 1-1 với server |
| require_approval | BOOLEAN DEFAULT false | true = join phải qua duyệt |
| welcome_message | TEXT NULL | Mẫu tin nhắn chào (e.g. "Welcome {username}!") |
| welcome_channel_id | BIGINT NULL FK channels | Kênh gửi tin chào; NULL = không gửi |
| allow_invite_role | VARCHAR(32) | owner_only \| members \| none (ai được tạo invite) |
| default_role_id | BIGINT NULL FK roles | Role gán khi join (NULL = không gán) |
| updated_at | TIMESTAMP | |

- **require_approval**: true → user join (tự join hoặc accept invite) tạo bản ghi “yêu cầu join”, chờ owner/admin duyệt; false → join trực tiếp vào `server_members`.
- **welcome_message** + **welcome_channel_id**: Khi user chính thức join (sau khi duyệt hoặc join trực tiếp), nếu có cấu hình thì tạo 1 message trong kênh (system/bot author tùy nghiệp vụ).

### 3.2 Join requests (mới)

Dùng khi `require_approval = true`.

**Bảng `server_join_requests`**:

| Cột | Kiểu | Mô tả |
|-----|------|--------|
| id | BIGSERIAL PK | |
| server_id | BIGINT NOT NULL FK servers | |
| user_id | BIGINT NOT NULL FK users | Người xin join |
| status | VARCHAR(16) NOT NULL | PENDING, APPROVED, REJECTED |
| message | TEXT NULL | Lời nhắn (optional) từ user |
| invited_by_invite_id | BIGINT NULL FK server_invites | Nếu join qua invite |
| reviewed_by | BIGINT NULL FK users | Người duyệt |
| reviewed_at | TIMESTAMP NULL | |
| review_note | TEXT NULL | Ghi chú khi duyệt (từ chối/approve) |
| created_at | TIMESTAMP NOT NULL | |

- Unique: (server_id, user_id) where status = 'PENDING' (một user chỉ 1 request đang chờ/1 server).
- Khi APPROVED: insert vào `server_members`, có thể gửi welcome message, audit log.

### 3.3 Invite (đã có, bổ sung dùng)

- `server_invites`: code, server_id, created_by, uses, max_uses, expire_at, temporary.
- Có thể thêm: `invite_type` (link \| direct), `target_user_id` (nếu direct invite) — tùy product.

---

## 4. Luồng nghiệp vụ

### 4.1 User tự join (không qua invite)

1. User gọi **POST /servers/{serverId}/join** (có thể kèm message).
2. Kiểm tra: đã member → 409; bị ban → 403; server không tồn tại → 404.
3. Đọc `server_settings.require_approval`:
   - **false**: Insert `server_members`, (optional) gửi welcome message → 201.
   - **true**: Insert `server_join_requests` (status=PENDING) → 202 Accepted (đã gửi yêu cầu).

### 4.2 User join qua invite (accept link)

1. User có link/code → **GET /invites/{code}** (resolve thông tin server, người mời).
2. **POST /invites/{code}/accept** (hoặc POST /servers/join-by-invite với body { code }).
3. Kiểm tra: invite hợp lệ (còn hạn, còn uses, không banned); đã member → 409.
4. Đọc `require_approval`:
   - **false**: Insert `server_members`, tăng `server_invites.uses`, (optional) welcome message → 201.
   - **true**: Insert `server_join_requests` (status=PENDING, invited_by_invite_id = invite.id), tăng uses (hoặc chỉ tăng khi approve — tùy product) → 202.

### 4.3 Mời user (tạo invite)

1. **POST /servers/{serverId}/invites**: body { max_uses?, expire_at?, temporary? }.
2. Kiểm tra quyền theo `server_settings.allow_invite_role` (owner / members).
3. Tạo bản ghi `server_invites` (code unique), trả về link + code.

(Optional) **Mời trực tiếp theo user**: POST /servers/{serverId}/invites/direct với { userId } → tạo invite đặc biệt hoặc gửi thông báo mời; khi user accept thì như accept invite.

### 4.4 Duyệt yêu cầu join

1. **GET /servers/{serverId}/join-requests**: list PENDING (chỉ owner/admin).
2. **PATCH /servers/{serverId}/join-requests/{requestId}**: body { action: APPROVE \| REJECT, review_note? }.
3. Nếu APPROVE: insert `server_members`, cập nhật request status, (optional) welcome message, audit log.

### 4.5 Gửi tin nhắn chào mừng

- Trigger: Sau khi user chính thức có trong `server_members` (join trực tiếp hoặc sau khi duyệt).
- Nếu `welcome_message` và `welcome_channel_id` đều có: tạo 1 message trong channel đó, content = replace placeholder trong welcome_message (e.g. `{username}`, `{serverName}`).
- Author: system hoặc bot user (tùy bạn chọn).

---

## 5. API đề xuất (REST)

| Method | Path | Mô tả |
|--------|------|--------|
| POST | /servers/{id}/join | Tự join (body: message?) → 201 member / 202 pending |
| GET | /servers/{id}/join-requests | List yêu cầu join (PENDING) — owner/admin |
| PATCH | /servers/{id}/join-requests/{requestId} | Duyệt/từ chối (body: action, review_note?) |
| GET | /servers/{id}/settings | Lấy settings (owner/admin) |
| PUT hoặc PATCH | /servers/{id}/settings | Cập nhật settings (owner) |
| POST | /servers/{id}/invites | Tạo invite (body: max_uses?, expires_at?, temporary?) |
| GET | /invites/{code} | Resolve invite (server info, không cần auth) |
| POST | /invites/{code}/accept | Chấp nhận invite → join hoặc tạo join request |

---

## 6. DB migration cần thêm

- **server_settings**: server_id (PK), require_approval, welcome_message, welcome_channel_id, allow_invite_role, default_role_id, updated_at.
- **server_join_requests**: id, server_id, user_id, status, message, invited_by_invite_id, reviewed_by, reviewed_at, review_note, created_at; unique (server_id, user_id) + check status cho PENDING; index (server_id, status).

---

## 7. Edge cases & mở rộng

- **Ban**: Trước khi join/accept invite kiểm tra `bans` (server_id, user_id).
- **Invite hết hạn / max_uses**: Validate trong accept.
- **Spam join request**: Một user chỉ 1 PENDING/server; có thể thêm rate limit.
- **Welcome message**: Placeholder chuẩn: {username}, {serverName}, {mention} (mention user trong channel).
- **Phân quyền**: Sau này gắn với role (e.g. MANAGE_SERVER, APPROVE_JOIN) thay vì chỉ owner.

Bạn có thể triển khai theo thứ tự: (1) Migration + ServerSettings entity, (2) Join + JoinRequest, (3) Invite accept, (4) Welcome message.

---

## 8. Đã có trong codebase

- **Migration**: `V11__create_server_settings_and_join_requests.sql` – bảng `server_settings`, `server_join_requests`.
- **Entities**: `ServerSettings`, `ServerJoinRequest`; enums `JoinRequestStatus`, `AllowInviteRole`.
- **Repositories**: `ServerSettingsRepository`, `ServerJoinRequestRepository`.

**Bước tiếp theo gợi ý**: Tạo service (vd. `ServerJoinService`, `ServerInviteService`) và controller endpoints như bảng API trên; khi tạo server mới có thể insert mặc định một dòng vào `server_settings` (require_approval=false, allow_invite_role=members).

---

## 9. Tối ưu cho nhiều server, nhiều user, nhiều invite

### 9.1 Đánh giá cách lưu hiện tại

| Bảng | Cách lưu | Scale |
|------|----------|--------|
| **server_settings** | 1 row/server, PK = server_id | ✅ O(servers). Lookup theo server O(1). |
| **server_join_requests** | 1 row/request, lưu cả PENDING + lịch sử APPROVED/REJECTED | ⚠️ Số dòng ≈ (số server × số user từng xin join). Cần index + có thể archive. |
| **server_invites** | 1 row/invite, code UNIQUE | ✅ Tra cứu theo code O(1). Nhiều invite/server → cần index theo server_id. |
| **server_members** | 1 row/(server, user), đã có UNIQUE + index | ✅ Đủ tốt. |

**Kết luận**: Cách lưu (chuẩn hóa, bảng riêng, FK) là ổn. Để scale tốt cần **đủ index** và **chính sách dữ liệu cũ** (archive/TTL) cho join_requests.

### 9.2 Index đã có / nên thêm

- **server_join_requests**
  - Đã có: `(server_id, user_id) WHERE status='PENDING'` (unique), `(server_id, status)`, `(user_id)`.
  - Nên thêm (đã thêm trong V12): `(user_id, status)` cho “danh sách yêu cầu đang chờ của tôi”, `(server_id, created_at DESC)` cho list + phân trang theo server.
- **server_invites**
  - Đã có: UNIQUE(code).
  - Nên thêm (V12): `(server_id)` cho “list invite của server”, `(expire_at)` (có thể partial WHERE expire_at IS NOT NULL) cho job dọn invite hết hạn.

Migration **V12** thêm các index trên để hỗ trợ nhiều server, nhiều user, nhiều invite.

### 9.3 Khi số lượng rất lớn

- **server_join_requests**  
  - Giữ lại PENDING; với APPROVED/REJECTED có thể: xóa sau N ngày, hoặc chuyển sang bảng `server_join_requests_archive` (hoặc partition theo `created_at` / theo status) để bảng chính không phình.
- **server_invites**  
  - Job định kỳ xóa (hoặc ẩn) invite hết hạn để bảng không lớn vô hạn.
- **Rate limit**  
  - Giới hạn: tạo invite / gửi join request theo user (và theo server) để tránh spam và tải không cần thiết.

### 9.4 Tóm tắt

- **Lưu như hiện tại là ổn**: bảng chuẩn hóa, FK rõ ràng, không thừa dữ liệu.
- **Đã bổ sung index** (V12) cho truy vấn theo server, theo user, theo trạng thái và phân trang.
- **Scale thêm**: archive/TTL cho join request đã xử lý + dọn invite hết hạn + rate limit.
