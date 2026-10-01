# Triển khai ChatApp miễn phí trên Render + Aiven

Thiết lập này sử dụng Render cho các trang web tĩnh và backend chạy bằng Docker, còn Aiven được dùng cho cơ sở dữ liệu MySQL. Giải pháp này phù hợp cho bản demo hoặc dự án cá nhân: Render sẽ tạm dừng các dịch vụ web miễn phí sau 15 phút không có hoạt động, và quá trình khởi động lại sau đó có thể mất khoảng một phút. Gói MySQL miễn phí của Aiven cung cấp 1 CPU, 1 GB RAM và 1 GB dung lượng lưu trữ, đồng thời có thể tự động tắt sau một thời gian dài không hoạt động. Hãy kiểm tra các giới hạn hiện tại tại: [Render Free](https://render.com/docs/free), [Aiven MySQL Free](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier).

## 1. Tạo cơ sở dữ liệu

1. Tạo tài khoản Aiven và thiết lập một instance **MySQL Free**.
2. Chờ cho đến khi trạng thái hiển thị là `Running`, sau đó mở phần thông tin kết nối.
3. Ghi lại các thông tin: host, port, tên cơ sở dữ liệu, tên người dùng và mật khẩu. Hãy bảo mật các thông tin này.

Aiven cũng cung cấp ví dụ về kết nối JDBC; dự án này hỗ trợ chế độ TLS thông qua biến môi trường `DB_SSL_MODE=REQUIRED`. Xem thêm tại [hướng dẫn kết nối Java của Aiven](https://aiven.io/docs/products/mysql/howto/connect-with-java).

## 2. Triển khai backend

1. Kết nối kho mã nguồn (repository) trên GitHub với Render và tạo một **Web Service**.
2. Chọn đúng kho mã nguồn và nhánh (branch) cần triển khai.
3. Thiết lập **Root Directory** (Thư mục gốc) là `chat_app`. Render sẽ tự động nhận diện tệp `Dockerfile` nằm trong thư mục này.
4. Chọn gói **Free** (Miễn phí) để chạy bản demo. 5. Thêm các biến môi trường này với giá trị Aiven:

```văn bản
DB_HOST=<máy chủ Aiven>
DB_PORT=<Cổng Aiven>
DB_NAME=<Cơ sở dữ liệu Aiven>
DB_USERNAME=<Tên người dùng Aiven>
DB_PASSWORD=<Mật khẩu aiven>
DB_SSL_MODE=BẮT BUỘC
JWT_SECRET=<Cung cấp chuỗi ngẫu nhiên, tối thiểu 32 byte>
JWT_EXPIATION=86400000
JPA_SHOW_SQL=sai
CHAT_ALLOWED_ORIGINS=https://<ten-frontend>.onrender.com
```

6. Tạo dịch vụ lấy tên miền Backend, dạng `https://<ten-backend>.onrender.com`.

Ứng dụng lắng nghe cổng Render cấp độ qua biến `PORT`.

## 3. Triển khai giao diện người dùng

1. Tạo **Trang web tĩnh** từ cùng một kho lưu trữ.
2. Đặt **Thư mục gốc** thay vì `Frontend_chat_app`.
3. Đặt **Lệnh xây dựng** thành:

```sh
printf "window.CHAT_API_BASE_URL = '%s';\n" "$CHAT_API_BASE_URL" > js/config.js
```

4. Đặt **Xuất bản thư mục** thành `.`.
5. Bạn có thể tìm thấy `CHAT_API_BASE_URL` trong phần phụ trợ của miền, xem `https://<ten-backend>.onrender.com`.
6. Triển khai; Kết xuất giao diện người dùng miền cấp dưới dạng `https://<ten-frontend>.onrender.com`.

Sau khi có Frontend tên miền, cập nhật `CHAT_ALLOWED_ORIGINS` trên Backend with right Origin FE (`https://...`, không có dấu `/` end), sau đó triển khai lại Backend.

## 4. Kiểm tra

Mở giao diện miền, tạo tài khoản, gửi tin nhắn, tải lại trang và thử kết nối thời gian thực. Nếu CORS bị lỗi, hãy kiểm tra `CHAT_ALLOWED_ORIGINS`. Bên cạnh MySQL, bạn có thể sử dụng máy chủ/cổng/thông tin xác thực để tìm `DB_SSL_MODE=REQUIRED`.

Các trang web tĩnh và dịch vụ web miễn phí hiện được cung cấp miễn phí trên Render, nhưng các dịch vụ web miễn phí có giới hạn và không được Render khuyến khích cho sản xuất. [Giới hạn dịch vụ miễn phí kết xuất](https://render.com/docs/free).