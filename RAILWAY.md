# Deploy ChatApp lên Railway

Railway tạo các service độc lập từ monorepo; không chạy `docker-compose.yml` trực tiếp. Tài liệu hiện tại: [Deploy a Docker Compose App](https://docs.railway.com/guides/docker-compose), [Monorepo](https://docs.railway.com/deployments/monorepo).

## 1. Đẩy mã nguồn lên GitHub

Commit và push repo này lên GitHub. Railway kết nối repo để tự deploy khi có commit mới.

## 2. Tạo project và MySQL

1. Tạo project mới trong Railway.
2. Chọn **Add Service → Database → MySQL**.
3. Chờ MySQL deploy xong. Giữ database private; backend cùng project kết nối qua private networking.

Railway MySQL cung cấp `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE` trong service variables. Xem [MySQL trên Railway](https://docs.railway.com/databases/mysql).

## 3. Tạo Backend service

1. Thêm service từ GitHub repo.
2. Mở **Settings → Source → Root Directory** và đặt `/chat_app`.
3. Đảm bảo Railway phát hiện `Dockerfile` trong thư mục đó.
4. Trong **Variables**, thêm các biến sau; thay `MySQL` bằng đúng tên service database của bạn:

   ```text
   DB_HOST=${{MySQL.MYSQLHOST}}
   DB_PORT=${{MySQL.MYSQLPORT}}
   DB_NAME=${{MySQL.MYSQLDATABASE}}
   DB_USERNAME=${{MySQL.MYSQLUSER}}
   DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   JWT_SECRET=<chuỗi ngẫu nhiên riêng, ít nhất 32 bytes>
   JWT_EXPIRATION=86400000
   JPA_SHOW_SQL=false
   ```

5. Deploy Backend. Sau khi deploy thành công, mở **Settings → Networking → Public Networking → Generate Domain**. Ghi lại domain backend, ví dụ `backend-production-xxxx.up.railway.app`.

Backend đọc `PORT` Railway tự cấp qua `server.port=${PORT:8080}`.

## 4. Tạo Frontend service

1. Thêm service thứ hai từ cùng GitHub repo.
2. Đặt **Root Directory** thành `/Frontend_chat_app` để Railway dùng Dockerfile trong thư mục FE.
3. Thêm biến `CHAT_API_BASE_URL` vào Frontend service:

   ```text
   CHAT_API_BASE_URL=https://<domain-backend-cua-ban>
   ```

   Đây là build argument mà Dockerfile dùng để tạo `js/config.js`. Sau khi đổi biến này cần redeploy Frontend.

4. Deploy Frontend, rồi tạo public domain trong **Settings → Networking**. Ghi lại origin FE, ví dụ `https://frontend-production-xxxx.up.railway.app`.

Nginx dùng `/etc/nginx/templates/default.conf.template` để listen trên biến `PORT` Railway tự cấp.

## 5. Cho phép Frontend gọi Backend

Quay lại **Backend → Variables**, đặt:

```text
CHAT_ALLOWED_ORIGINS=https://<domain-frontend-cua-ban>
```

Nếu có custom domain, dùng origin người dùng thực sự mở, gồm `https://` và không có path, ví dụ `https://chat.example.com`. Có thể thêm nhiều origin cách nhau bằng dấu phẩy. Deploy lại Backend để nhận biến mới.

## 6. Kiểm tra

- Mở domain Frontend và đăng ký tài khoản.
- Kiểm tra đăng nhập, tải lại trang chat và gửi tin nhắn realtime.
- Nếu API bị CORS chặn, đối chiếu origin của trang với `CHAT_ALLOWED_ORIGINS`.
- Nếu WebSocket không kết nối, xác nhận `CHAT_API_BASE_URL` dùng `https://` và backend domain hoạt động; client tự đổi sang `wss://`.
- Xem deployment logs trên từng service nếu backend không kết nối được database.

Không thêm public domain cho MySQL. Railway cung cấp domain riêng cho Frontend và Backend; API/WebSocket dùng domain public Backend, còn database chỉ được backend truy cập qua private networking.
