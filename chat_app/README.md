# Rinchat Backend

Spring Boot backend cung cấp REST API và WebSocket/STOMP. Frontend nằm riêng tại `../Frontend_chat_app`.

## Chạy

Yêu cầu JDK 21 và MySQL 8. Tạo database `chat_app`, cấu hình `.env` từ `.env.example`, rồi chạy:

```powershell
.\mvnw.cmd spring-boot:run
```

API và WebSocket mặc định chạy trên `http://localhost:8080` và `/ws`. Frontend mặc định được phục vụ riêng ở `http://localhost:5500`; CORS và WebSocket origins được cấu hình bởi `CHAT_ALLOWED_ORIGINS`.

Để chạy kiểm thử:

```powershell
.\mvnw.cmd clean test
```
