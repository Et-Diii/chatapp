# Rinchat

Ứng dụng chat tách thành hai phần độc lập:

- `chat_app/`: Backend Java 21, Spring Boot, REST API và WebSocket/STOMP.
- `Frontend_chat_app/`: Frontend HTML/CSS/JavaScript.

## Deploy bằng Docker

Yêu cầu Docker Engine và Docker Compose. Từ thư mục gốc, sao chép `.env.docker.example` thành `.env`, đặt mật khẩu database và JWT secret mạnh, rồi chạy:

```powershell
docker compose up --build -d
```

Mở `http://localhost` (hoặc domain/IP máy chủ). Frontend Nginx chuyển tiếp REST API và WebSocket đến backend; MySQL lưu dữ liệu trong Docker volume `mysql_data`.

Trước khi deploy với domain, đặt `CHAT_ALLOWED_ORIGINS` trong `.env` thành origin đầy đủ, ví dụ `https://chat.example.com`, và cấu hình HTTPS ở reverse proxy/load balancer bên ngoài. Xem trạng thái bằng `docker compose ps`, log bằng `docker compose logs -f`, và dừng bằng `docker compose down`. Dữ liệu database được giữ lại khi chạy `down`; `docker compose down -v` sẽ xóa volume database.

## Deploy lên Railway

Railway cần ba service độc lập cho frontend, backend và MySQL. Hướng dẫn cấu hình chi tiết có trong [RAILWAY.md](RAILWAY.md).

## Chạy backend

Tạo database MySQL `chat_app`, cấu hình `chat_app/.env` dựa trên `.env.example`, sau đó:

```powershell
cd chat_app
.\mvnw.cmd spring-boot:run
```

## Chạy frontend

Mở terminal thứ hai:

```powershell
cd Frontend_chat_app
py -m http.server 5500
```

Mở `http://localhost:5500`. Frontend mặc định gọi backend tại `http://localhost:8080`; thay đổi địa chỉ tại `Frontend_chat_app/js/config.js` khi cần. CORS backend mặc định cho phép `localhost:5500` và `127.0.0.1:5500`; các origin khác cần được thêm vào `CHAT_ALLOWED_ORIGINS` trong `chat_app/.env`.
