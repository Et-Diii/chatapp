# Rinchat — Giao diện

Frontend thuần HTML, CSS và JavaScript. Thư mục này chạy độc lập với Spring Boot backend.

## Chạy

1. Đảm bảo backend đang chạy tại `http://localhost:8080`.
2. Mở PowerShell tại thư mục `Frontend_chat_app` và chạy:

   ```powershell
   py -m http.server 5500
   ```

3. Truy cập `http://localhost:5500`.

Nếu backend ở địa chỉ khác, đổi `CHAT_API_BASE_URL` trong `js/config.js`. Backend chỉ cho phép các origin liệt kê trong `CHAT_ALLOWED_ORIGINS` ở `chat_app/.env` (hoặc giá trị mặc định trong `application.properties`). Khi dùng địa chỉ FE khác, cập nhật biến đó, ví dụ `CHAT_ALLOWED_ORIGINS=http://localhost:5500,http://127.0.0.1:5500`.
