FROM nginx:alpine
ARG CHAT_API_BASE_URL=
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY . /usr/share/nginx/html
RUN echo "window.CHAT_API_BASE_URL = '${CHAT_API_BASE_URL}';" > /usr/share/nginx/html/js/config.js
EXPOSE 80
