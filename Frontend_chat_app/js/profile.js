import { apiData, getCurrentUser, avatarUrl, nameOf, requireAuth, setupSharedUi, showMessage, escapeHtml } from './api.js';
import { connectWebSocket } from './websocket.js';

if (requireAuth()) { setupSharedUi(); void loadProfile(); }

async function loadProfile() {
  let user = getCurrentUser() || {};
  try { user = await apiData('/api/users/me') || user; localStorage.setItem('user', JSON.stringify(user)); }
  catch (error) { showMessage(document.querySelector('[data-error]'), error.message); }
  document.querySelector('[data-profile-name]').textContent = nameOf(user);
  document.querySelector('[data-profile-username]').textContent = user.username ? `@${user.username}` : '';
  document.querySelector('[data-profile-handle]').textContent = user.username ? `@${user.username}` : '—';
  document.querySelector('[data-profile-email]').textContent = user.email || '—';
  const status = String(user.status || 'OFFLINE').toUpperCase();
  const statusEl = document.querySelector('[data-profile-status]');
  statusEl.classList.toggle('is-offline', status !== 'ONLINE');
  statusEl.lastChild.textContent = status === 'ONLINE' ? ' Trực tuyến' : ' Ngoại tuyến';
  connectWebSocket(() => {
    statusEl.classList.remove('is-offline');
    statusEl.lastChild.textContent = ' Trực tuyến';
  });
  const avatar = document.querySelector('[data-profile-avatar]');
  avatar.src = avatarUrl(user) || `https://placehold.co/96x96/dff1ec/176b63?text=${encodeURIComponent(nameOf(user).slice(0, 1).toUpperCase())}`;
  avatar.alt = `Ảnh đại diện của ${nameOf(user)}`;
  const form = document.querySelector('[data-profile-form]');
  form.elements.displayName.value = user.displayName || user.name || '';
  form.elements.avatarUrl.value = avatarUrl(user);
  form.addEventListener('submit', async event => {
    event.preventDefault();
    const error = form.querySelector('[data-error]');
    const success = form.querySelector('[data-success]');
    showMessage(error, ''); showMessage(success, '');
    const displayName = form.elements.displayName.value.trim();
    const url = form.elements.avatarUrl.value.trim();
    if (!displayName) return showMessage(error, 'Vui lòng nhập tên hiển thị.');
    const button = form.querySelector('button[type="submit"]'); button.disabled = true;
    try {
      const updated = await apiData('/api/users/me', { method: 'PUT', body: JSON.stringify({ displayName, avatarUrl: url || null }) });
      user = { ...user, ...updated, displayName, avatarUrl: url || null };
      localStorage.setItem('user', JSON.stringify(user));
      document.querySelector('[data-profile-name]').textContent = nameOf(user);
      document.querySelector('[data-profile-username]').textContent = user.username ? `@${user.username}` : '';
      document.querySelector('[data-current-name]') && (document.querySelector('[data-current-name]').textContent = nameOf(user));
      avatar.src = url || `https://placehold.co/96x96/dff1ec/176b63?text=${encodeURIComponent(nameOf(user).slice(0, 1).toUpperCase())}`;
      document.querySelectorAll('[data-current-avatar]').forEach(image => image.src = avatar.src);
      showMessage(success, 'Đã cập nhật hồ sơ.', 'success');
    } catch (err) { showMessage(error, err.message); }
    finally { button.disabled = false; }
  });
}
