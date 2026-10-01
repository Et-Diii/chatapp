import { apiData, showMessage, setBusy } from './api.js';
import { validateEmail, redirectIfSignedIn } from './auth.js';

redirectIfSignedIn();
const form = document.querySelector('[data-register-form]');
form.addEventListener('submit', async event => {
  event.preventDefault();
  const data = new FormData(form);
  const username = String(data.get('username') || '').trim();
  const email = String(data.get('email') || '').trim();
  const displayName = String(data.get('displayName') || '').trim();
  const password = String(data.get('password') || '');
  const confirmation = String(data.get('confirmPassword') || '');
  const error = form.querySelector('[data-error]');
  showMessage(error, '');
  if (!username || !email || !displayName || !password || !confirmation) return showMessage(error, 'Vui lòng điền đầy đủ thông tin.');
  if (!validateEmail(email)) return showMessage(error, 'Vui lòng nhập địa chỉ email hợp lệ.');
  if (password.length < 8) return showMessage(error, 'Mật khẩu cần có ít nhất 8 ký tự.');
  if (password !== confirmation) return showMessage(error, 'Mật khẩu xác nhận chưa khớp.');
  const button = form.querySelector('button[type="submit"]');
  setBusy(button, true, 'Đang tạo tài khoản…');
  try {
    await apiData('/api/auth/register', { method: 'POST', body: JSON.stringify({ username, email, displayName, password }) });
    location.replace('/login.html?registered=1');
  } catch (err) { showMessage(error, err.message); setBusy(button, false); }
});
