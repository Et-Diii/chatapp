export const getToken = () => localStorage.getItem('token');
export const API_BASE_URL = (window.CHAT_API_BASE_URL || '').replace(/\/$/, '');
export const getCurrentUser = () => {
  try { return JSON.parse(localStorage.getItem('user') || 'null'); } catch { return null; }
};

export async function apiRequest(url, options = {}) {
  const headers = { ...(options.body && !(options.body instanceof FormData) ? { 'Content-Type': 'application/json' } : {}), ...(options.headers || {}) };
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${url}`, { ...options, headers });
  } catch {
    throw new Error('Could not connect to ChatApp. Check your connection and try again.');
  }
  if (response.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    if (!location.pathname.endsWith('/login.html')) location.replace('/login.html');
    throw new Error('Your session has expired. Please log in again.');
  }
  return response;
}

export async function apiData(url, options) {
  const response = await apiRequest(url, options);
  if (!response.ok) {
    let message = `Request failed (${response.status}).`;
    try { const body = await response.json(); message = body.message || body.error || message; } catch { /* Empty response. */ }
    throw new Error(message);
  }
  if (response.status === 204) return null;
  const type = response.headers.get('content-type') || '';
  return type.includes('application/json') ? response.json() : null;
}

export const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
export const initials = value => String(value || '?').trim().split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase();
export const avatarUrl = person => person?.avatarUrl || person?.avatar || person?.profileImageUrl || '';
export const nameOf = person => person?.displayName || person?.name || person?.username || 'ChatApp user';
export const userIdOf = person => person?.id ?? person?.userId;

export function avatarMarkup(person, className = 'avatar') {
  const url = avatarUrl(person);
  const title = escapeHtml(nameOf(person));
  return `<img class="${className}" src="${escapeHtml(url || `https://placehold.co/96x96/e9e7ff/6559d9?text=${encodeURIComponent(initials(nameOf(person)))}`)}" alt="${title}" loading="lazy" data-avatar-fallback>`;
}

export function showMessage(element, message, kind = 'error') {
  if (!element) return;
  element.textContent = message || '';
  element.classList.toggle('is-success', kind === 'success');
}

export function setBusy(button, busy, label = 'Please wait…') {
  if (!button) return;
  if (busy) {
    button.dataset.originalLabel = button.querySelector('[data-button-label]')?.textContent || button.textContent.trim();
    button.disabled = true;
    const target = button.querySelector('[data-button-label]') || button;
    target.textContent = label;
  } else {
    button.disabled = false;
    const target = button.querySelector('[data-button-label]') || button;
    target.textContent = button.dataset.originalLabel || target.textContent;
  }
}

export function requireAuth() {
  if (!getToken()) {
    location.replace('/login.html');
    return false;
  }
  return true;
}

export function setupSharedUi() {
  void import('./websocket.js').then(({ connectWebSocket, disconnectWebSocket }) => {
    connectWebSocket();
    window.addEventListener('pagehide', disconnectWebSocket, { once: true });
  });
  document.querySelectorAll('[data-logout]').forEach(button => button.addEventListener('click', async () => {
    const { disconnectWebSocket } = await import('./websocket.js');
    disconnectWebSocket();
    try { await apiRequest('/api/auth/logout', { method: 'POST' }); } catch { /* Local logout still completes if the server is unavailable. */ }
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    location.replace('/login.html');
  }));
  const user = getCurrentUser();
  if (user) {
    document.querySelectorAll('[data-current-name]').forEach(el => { el.textContent = nameOf(user); });
    document.querySelectorAll('[data-current-avatar]').forEach(img => {
      const url = avatarUrl(user);
      img.src = url || `https://placehold.co/96x96/e9e7ff/6559d9?text=${encodeURIComponent(initials(nameOf(user)))}`;
      img.alt = `${nameOf(user)} avatar`;
    });
  }
  document.addEventListener('error', event => {
    const img = event.target;
    if (img instanceof HTMLImageElement && img.hasAttribute('data-avatar-fallback') && !img.dataset.fallbackDone) {
      img.dataset.fallbackDone = 'true';
      img.src = `https://placehold.co/96x96/e9e7ff/6559d9?text=${encodeURIComponent(initials(img.alt))}`;
    }
  }, true);
}

export async function endpointList(...urls) {
  let lastError;
  for (const url of urls) {
    try {
      const data = await apiData(url);
      return Array.isArray(data) ? data : data?.content || data?.items || data?.data || [];
    } catch (error) {
      if (error.message.startsWith('Your session')) throw error;
      lastError = error;
    }
  }
  throw lastError || new Error('Could not load data.');
}
