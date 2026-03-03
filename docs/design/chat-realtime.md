 # Chat Realtime – API & WebSocket

 Tài liệu này mô tả luồng chat realtime cho FE, bao gồm REST API, WebSocket/STOMP endpoint, topic subscribe, payload và auth.

 ## 1. Authentication

 - Tất cả kết nối và gửi message đều yêu cầu **JWT access token** hợp lệ.
 - Với **REST API**: dùng header `Authorization: Bearer <JWT>`.
 - Với **WebSocket/STOMP**:
   - Có thể truyền token qua query param: `/ws?token=<JWT>`.
   - Hoặc qua header `Authorization: Bearer <JWT>` khi gọi `connect()` của STOMP client (tùy thư viện).

 WebSocket handshake interceptor sẽ:

 - Validate token.
 - Lấy `userId` từ token, load user details.
 - Thiết lập `SecurityContext` cho session WS.

 Nếu token không hợp lệ → **kết nối WS bị từ chối**.

 ## 2. WebSocket / STOMP

 - **Endpoint SockJS/STOMP**: `/ws`
 - **Application destination prefix (client → server)**: `/app`
 - **Broker destination prefix (server → client)**: `/topic`

 ### 2.1. Kết nối STOMP (ví dụ FE)

 ```javascript
 import SockJS from 'sockjs-client';
 import { Client } from '@stomp/stompjs';

 const token = '<JWT>';

 const socket = new SockJS('/ws?token=' + encodeURIComponent(token));
 const stompClient = new Client({
   webSocketFactory: () => socket,
   connectHeaders: {
     Authorization: `Bearer ${token}` // optional, tùy chọn header thay cho query param
   }
 });

 stompClient.onConnect = () => {
   console.log('STOMP connected');
 };

 stompClient.onStompError = (frame) => {
   console.error('Broker error', frame);
 };

 stompClient.activate();
 ```

 ## 3. Subscribe nhận message realtime

 Message realtime được publish lên topic STOMP theo pattern:

 - **Destination**: `/topic/servers/{serverId}/channels/{channelId}`

 Ví dụ FE subscribe:

 ```javascript
 const serverId = 1;
 const channelId = 10;

 stompClient.subscribe(
   `/topic/servers/${serverId}/channels/${channelId}`,
   (frame) => {
     const event = JSON.parse(frame.body);
     // event là ChatMessageEvent (xem schema bên dưới)
   }
 );
 ```

 ### 3.1. Payload ChatMessageEvent

 Backend gửi về object `ChatMessageEvent` với format JSON:

 ```json
 {
   "eventType": "MESSAGE_CREATED",
   "serverId": 1,
   "channelId": 10,
   "authorId": 123,
   "content": "Hello world",
   "createdAt": "2026-03-03T12:34:56.789Z"
 }
 ```

 Ý nghĩa field:

 - `eventType`: hiện tại `"MESSAGE_CREATED"`.
 - `serverId`: ID server chứa channel.
 - `channelId`: ID channel.
 - `authorId`: ID user gửi message.
 - `content`: nội dung text.
 - `createdAt`: thời điểm server tạo event (UTC, ISO-8601).

 ## 4. Gửi message qua REST API

 **Mục đích**: gửi message realtime mà không cần WebSocket (hoặc dùng như fallback).

 - **Method**: `POST`
 - **URL**: `/api/v1/servers/{serverId}/channels/{channelId}/messages/realtime`
 - **Auth**: `Authorization: Bearer <JWT>`

 ### 4.1. Request body

 ```json
 {
   "content": "Hello world"
 }
 ```

 - `content`: string, bắt buộc, không được rỗng.

 ### 4.2. Response

 - HTTP status: `202 Accepted`
 - Body: `ChatMessageEvent` giống format ở mục 3.1

 ```json
 {
   "eventType": "MESSAGE_CREATED",
   "serverId": 1,
   "channelId": 10,
   "authorId": 123,
   "content": "Hello world",
   "createdAt": "2026-03-03T12:34:56.789Z"
 }
 ```

 ## 5. Gửi message qua WebSocket (STOMP)

 **Mục đích**: gửi message trực tiếp trên WS, latency thấp hơn, phù hợp UI chat.

 - **Destination (client → server)**: `/app/chat.sendMessage`
 - **Payload (JSON)** – mapping với DTO `ChatWsSendMessage`:

 ```json
 {
   "serverId": 1,
   "channelId": 10,
   "content": "Hello world"
 }
 ```

 - `serverId`: `Long`, bắt buộc.
 - `channelId`: `Long`, bắt buộc.
 - `content`: `String`, bắt buộc, không rỗng.

 ### 5.1. Ví dụ FE send

 ```javascript
 stompClient.send(
   '/app/chat.sendMessage',
   {},
   JSON.stringify({
     serverId: 1,
     channelId: 10,
     content: 'Hello world'
   })
 );
 ```

 - Hàm này **không trả về** data trực tiếp trong callback.
 - Sau khi server xử lý và publish Kafka → STOMP broker → FE nhận message qua subscription `/topic/servers/{serverId}/channels/{channelId}` như mục 3.

 ## 6. Luồng tổng quan backend

 1. FE gửi message:
    - Qua REST: `POST /api/v1/servers/{serverId}/channels/{channelId}/messages/realtime`
    - Hoặc qua WS: `SEND /app/chat.sendMessage` với payload tương ứng.
 2. Controller/WS controller gọi `ChatRealtimeService.sendMessage(serverId, channelId, request)`.
 3. Service:
    - Lấy `currentUserId` từ `SecurityContext`.
    - Validate channel:
      - Tồn tại.
      - Thuộc đúng `serverId`.
      - Kiểu channel là `text`.
    - Kiểm tra user:
      - Là owner server, hoặc
      - Là member server.
    - Build `ChatMessageEvent`.
    - Serialize JSON và gửi vào Kafka topic `chat-messages` với key `{serverId}:{channelId}`.
 4. `ChatMessageKafkaListener`:
    - Lắng nghe Kafka topic `chat-messages`.
    - Parse JSON về `ChatMessageEvent`.
    - Gửi lên STOMP destination: `/topic/servers/{serverId}/channels/{channelId}`.
 5. FE (đang subscribe) nhận event và update UI.

 ## 7. Edge cases / lỗi quan trọng

 - **Token thiếu/invalid khi connect WS**:
   - Handshake bị reject, FE không connect được WebSocket.
 - **User không phải member server**:
   - Backend ném lỗi `AUTH_403_FORBIDDEN` (“You are not a member of this server”).
 - **Channel không tồn tại**:
   - Lỗi `RESOURCE_404_NOT_FOUND` (“Channel not found: {channelId}”).
 - **Channel không thuộc server**:
   - Lỗi `BUSINESS_400_ERROR` (“Channel does not belong to server {serverId}”).
 - **Channel không phải text**:
   - Lỗi `BUSINESS_400_ERROR` (“Cannot send text message to non-text channel”).
 - **Lỗi serialize JSON trước khi gửi Kafka**:
   - Lỗi `SYS_500_INTERNAL_ERROR` (“Failed to serialize chat message event”).

 ## 8. Gợi ý usage từ phía FE

 - Khi user mở 1 channel:
   - Connect STOMP (nếu chưa).
   - Subscribe `/topic/servers/{serverId}/channels/{channelId}`.
 - Khi user gửi message:
   - Ưu tiên dùng WS: `SEND /app/chat.sendMessage`.
   - Option: nếu WS lỗi/hết kết nối, fallback sang REST `POST /messages/realtime`.
 - Khi user rời channel:
   - Unsubscribe topic để tránh nhận thừa message.

 ## 9. Hướng phát triển thêm

 - Thêm `messageId` và các metadata (edited, pinned, attachments…) vào `ChatMessageEvent`.
 - Thêm cơ chế ACK/error riêng trên WS (ví dụ event `MESSAGE_SEND_FAILED`) để FE hiển thị trạng thái gửi thất bại.
 - Hỗ trợ multi-event types (`MESSAGE_UPDATED`, `MESSAGE_DELETED`, typing indicator, v.v.).

