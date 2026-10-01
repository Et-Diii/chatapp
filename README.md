# ChatApp

Ứng dụng chat tách thành hai phần độc lập:

- `chat_app/`: Backend Java 21, Spring Boot, REST API và WebSocket/STOMP.
- `Frontend_chat_app/`: Frontend HTML/CSS/JavaScript.

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
