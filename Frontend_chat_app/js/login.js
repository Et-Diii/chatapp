import { submitLogin, redirectIfSignedIn } from './auth.js';
import { showMessage, setBusy } from './api.js';

redirectIfSignedIn();
const form = document.querySelector('[data-login-form]');
if (new URLSearchParams(location.search).has('registered')) {
  const note = document.createElement('p'); note.className = 'form-success'; note.textContent = 'Tài khoản đã sẵn sàng. Hãy đăng nhập để tiếp tục.';
  form.before(note);
}
form.addEventListener('submit', async event => {
  event.preventDefault();
  const fields = new FormData(form);
  const identifier = String(fields.get('identifier') || '').trim();
  const password = String(fields.get('password') || '');
  const error = form.querySelector('[data-error]');
  const button = form.querySelector('button[type="submit"]');
  showMessage(error, '');
  if (!identifier || !password) return showMessage(error, 'Nhập email hoặc tên tài khoản và mật khẩu.');
  setBusy(button, true, 'Đang đăng nhập…');
  try { await submitLogin(identifier, password); location.replace('/index.html'); }
  catch (err) { showMessage(error, err.message); setBusy(button, false); }
});
