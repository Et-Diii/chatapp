# Deploy ChatApp gratuit sur Render + Aiven

Cette configuration utilise Render pour les sites statiques et le backend Docker, et Aiven pour MySQL. C'est adapté à une démo ou un projet personnel : Render suspend les services web gratuits après 15 minutes sans trafic, puis le prochain réveil peut prendre environ une minute. Le MySQL gratuit d'Aiven fournit 1 CPU, 1 Go de RAM et 1 Go de stockage et peut être arrêté après une longue période d'inactivité. Consultez les limites actuelles : [Render Free](https://render.com/docs/free), [Aiven MySQL Free](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier).

## 1. Créer la base de données

1. Créez un compte Aiven et créez **MySQL Free**.
2. Attendez l'état `Running`, puis ouvrez les informations de connexion.
3. Notez host, port, database, username et password. Gardez les identifiants secrets.

Aiven donne aussi un exemple JDBC; ce projet accepte le mode TLS avec `DB_SSL_MODE=REQUIRED`. Voir [Aiven Java connection guide](https://aiven.io/docs/products/mysql/howto/connect-with-java).

## 2. Déployer le Backend

1. Connectez le dépôt GitHub à Render et créez un **Web Service**.
2. Choisissez le même dépôt et branche.
3. Réglez **Root Directory** sur `chat_app`. Render détectera le `Dockerfile` de ce dossier.
4. Choisissez **Free** pour une démo.
5. Ajoutez ces variables d'environnement avec les valeurs Aiven :

   ```text
   DB_HOST=<Aiven host>
   DB_PORT=<Aiven port>
   DB_NAME=<Aiven database>
   DB_USERNAME=<Aiven username>
   DB_PASSWORD=<Aiven password>
   DB_SSL_MODE=REQUIRED
   JWT_SECRET=<chuỗi ngẫu nhiên riêng, ít nhất 32 bytes>
   JWT_EXPIRATION=86400000
   JPA_SHOW_SQL=false
   CHAT_ALLOWED_ORIGINS=https://<ten-frontend>.onrender.com
   ```

6. Tạo service để lấy domain Backend, dạng `https://<ten-backend>.onrender.com`.

Ứng dụng lắng nghe cổng Render cấp qua biến `PORT`.

## 3. Deploy Frontend

1. Tạo **Static Site** từ cùng repo.
2. Đặt **Root Directory** thành `Frontend_chat_app`.
3. Đặt **Build Command** thành:

   ```sh
   printf "window.CHAT_API_BASE_URL = '%s';\n" "$CHAT_API_BASE_URL" > js/config.js
   ```

4. Đặt **Publish Directory** thành `.`.
5. Tạo biến `CHAT_API_BASE_URL` với domain backend, ví dụ `https://<ten-backend>.onrender.com`.
6. Deploy; Render cấp domain frontend dạng `https://<ten-frontend>.onrender.com`.

Sau khi domain Frontend có, cập nhật `CHAT_ALLOWED_ORIGINS` trên Backend bằng đúng origin FE (`https://...`, không có dấu `/` cuối), rồi redeploy Backend.

## 4. Kiểm tra

Mở domain Frontend, tạo tài khoản, gửi tin nhắn, tải lại trang và thử kết nối realtime. Nếu CORS lỗi, kiểm tra `CHAT_ALLOWED_ORIGINS`. Nếu MySQL không kết nối được, đối chiếu host/port/credentials của Aiven và đảm bảo `DB_SSL_MODE=REQUIRED`.

Static sites và Free web services hiện được cung cấp miễn phí trên Render, nhưng free web services có giới hạn và không được Render khuyến nghị cho production. [Render free service limits](https://render.com/docs/free).
