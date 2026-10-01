import { apiData, avatarMarkup, escapeHtml, initials, nameOf, userIdOf, endpointList } from './api.js';

export function renderPersonRow(person, actions = '') {
  return `<article class="person-row">${avatarMarkup(person)}<div class="person-info"><strong>${escapeHtml(nameOf(person))}</strong><span>@${escapeHtml(person.username || 'nguoidung')}</span></div><div class="person-actions">${actions}</div></article>`;
}

export function renderPersonCard(person, actions = '') {
  const online = person.online === true || person.isOnline === true || String(person.status || '').toUpperCase() === 'ONLINE';
  return `<article class="person-card">${avatarMarkup(person, 'avatar avatar-lg')}<div class="person-info"><strong>${escapeHtml(nameOf(person))}</strong><span>@${escapeHtml(person.username || 'nguoidung')}</span><span class="${online ? 'online-indicator' : 'offline-indicator'}">${online ? 'Trực tuyến' : 'Ngoại tuyến'}</span></div><div class="person-actions">${actions}</div></article>`;
}

export async function loadFriends() {
  return endpointList('/api/friends', '/api/users/me/friends');
}

export async function startConversation(friend) {
  const id = userIdOf(friend);
  const result = await apiData(`/api/conversations/private/${encodeURIComponent(id)}`, { method: 'POST' });
  const conversationId = result?.id ?? result?.conversationId;
  if (conversationId == null) throw new Error('Máy chủ không trả về mã cuộc trò chuyện.');
  location.href = `/chat.html?conversationId=${encodeURIComponent(conversationId)}`;
}

export function personFromRequest(request, kind = 'received') { return request.user || (kind === 'sent' ? request.receiver : request.sender) || request.from || request.requester || request; }

export function initialsSvgData(value) {
  const label = initials(value);
  return `data:image/svg+xml,${encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96"><rect width="100%" height="100%" rx="48" fill="#dff1ec"/><text x="50%" y="54%" text-anchor="middle" dominant-baseline="middle" fill="#176b63" font-family="Arial" font-size="32" font-weight="700">${label}</text></svg>`)}`;
}
