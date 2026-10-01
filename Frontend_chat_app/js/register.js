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
  if (!username || !email || !displayName || !password || !confirmation) return showMessage(error, 'Complete every field to create your account.');
  if (!validateEmail(email)) return showMessage(error, 'Enter a valid email address.');
  if (password.length < 8) return showMessage(error, 'Your password must be at least 8 characters.');
  if (password !== confirmation) return showMessage(error, 'Your passwords do not match.');
  const button = form.querySelector('button[type="submit"]');
  setBusy(button, true, 'Creating account…');
  try {
    await apiData('/api/auth/register', { method: 'POST', body: JSON.stringify({ username, email, displayName, password }) });
    location.replace('/login.html?registered=1');
  } catch (err) { showMessage(error, err.message); setBusy(button, false); }
});
