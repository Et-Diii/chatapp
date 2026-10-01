import { submitLogin, redirectIfSignedIn } from './auth.js';
import { showMessage, setBusy } from './api.js';

redirectIfSignedIn();
const form = document.querySelector('[data-login-form]');
if (new URLSearchParams(location.search).has('registered')) {
  const note = document.createElement('p'); note.className = 'form-success'; note.textContent = 'Your account is ready. Log in to continue.';
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
  if (!identifier || !password) return showMessage(error, 'Enter your email or username and password.');
  setBusy(button, true, 'Signing in…');
  try { await submitLogin(identifier, password); location.replace('/index.html'); }
  catch (err) { showMessage(error, err.message); setBusy(button, false); }
});
