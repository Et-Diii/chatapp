import { apiData, endpointList, escapeHtml, nameOf, avatarMarkup, getCurrentUser, requireAuth, setupSharedUi } from './api.js';
import { connectWebSocket, subscribeToMessages, disconnectWebSocket } from './websocket.js';

let conversationsById = new Map();
const unreadById = new Map();

if (requireAuth()) {
  setupSharedUi();
  void loadHome();
}

async function loadHome() {
  const container = document.querySelector('[data-conversations]');
  try {
    const conversations = await endpointList('/api/conversations');
    conversationsById = new Map(conversations.map(item => [String(item.id ?? item.conversationId), item]));
    document.querySelector('[data-conversation-count]').textContent = conversations.length ? String(conversations.length) : '';
    container.innerHTML = conversations.length ? conversations.map(conversation => {
      const person = conversation.otherUser || conversation.friend || conversation.participant || conversation.user || {};
      const id = conversation.id ?? conversation.conversationId;
      const preview = conversation.lastMessage?.content || conversation.lastMessage || conversation.preview || 'Bắt đầu trò chuyện';
      const unread = Number(conversation.unreadCount || 0);
      const online = String(person.status || '').toUpperCase() === 'ONLINE';
      const stamp = conversation.updatedAt ? new Date(conversation.updatedAt).toLocaleTimeString('vi-VN', {hour:'numeric',minute:'2-digit'}) : '';
      return `<a class="conversation-item" data-conversation-id="${escapeHtml(id)}" href="/chat.html?conversationId=${encodeURIComponent(id)}">${avatarMarkup(person)}<div class="person-info"><strong>${escapeHtml(nameOf(person))}<i class="presence-dot ${online ? 'is-online' : ''}" title="${online ? 'Trực tuyến' : 'Ngoại tuyến'}"></i>${stamp ? `<time class="conversation-time">${escapeHtml(stamp)}</time>` : ''}${unread ? `<span class="unread-count">${unread}</span>` : ''}</strong><span>${escapeHtml(typeof preview === 'string' ? preview : preview.content || '')}</span></div></a>`;
    }).join('') : '<p class="empty-state">Bạn chưa có cuộc trò chuyện nào. Hãy tìm bạn bè để bắt đầu nhé.</p>';
  } catch (error) { container.innerHTML = `<p class="empty-state">${escapeHtml(error.message)}</p>`; }

  connectWebSocket(() => subscribeToMessages(updateConversation), () => {});
  window.addEventListener('pagehide', disconnectWebSocket, { once: true });

  const requestsEl = document.querySelector('[data-request-preview]');
  try {
    const requests = await endpointList('/api/friend-requests/received', '/api/friend-requests?status=RECEIVED');
    requestsEl.innerHTML = requests.length ? requests.slice(0, 3).map(request => {
      const user = request.user || request.sender || request.from || request.requester || request;
      return `<a class="request-preview" href="/friends.html#requests">${avatarMarkup(user)}<span>${escapeHtml(nameOf(user))}</span></a>`;
    }).join('') : '<p class="muted small">Bạn chưa có lời mời mới.</p>';
  } catch (error) { requestsEl.innerHTML = `<p class="muted small">${escapeHtml(error.message)}</p>`; }
}

function updateConversation(message) {
  if (message.eventType === 'READ') return;
  const id = String(message.conversationId);
  const conversation = conversationsById.get(id);
  if (!conversation) return;
  const row = [...document.querySelectorAll('.conversation-item')].find(item => item.dataset.conversationId === id);
  if (!row) return;
  row.parentElement?.prepend(row);
  const preview = row.querySelector('.person-info > span');
  if (preview) preview.textContent = message.content || 'Tin nhắn mới';
  const time = row.querySelector('.conversation-time');
  if (time) time.textContent = new Date().toLocaleTimeString([], {hour:'numeric',minute:'2-digit'});
  const ownId = getCurrentUser()?.id;
  const unread = (unreadById.get(id) || Number(conversation.unreadCount || 0)) + (String(message.senderId) === String(ownId) ? 0 : 1);
  unreadById.set(id, unread);
  const label = row.querySelector('.person-info strong');
  if (label) {
    let badge = label.querySelector('.unread-count');
    if (unread > 0) {
      if (!badge) { badge = document.createElement('span'); badge.className = 'unread-count'; label.append(badge); }
      badge.textContent = String(unread);
    } else badge?.remove();
  }
}
