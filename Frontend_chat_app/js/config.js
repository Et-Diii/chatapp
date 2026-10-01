// Local static-server development uses the backend directly; Docker routes API and WebSocket through Nginx.
const localFrontendDev = ['localhost', '127.0.0.1'].includes(location.hostname) && location.port === '5500';
window.CHAT_API_BASE_URL = localFrontendDev ? 'http://localhost:8080' : '';
