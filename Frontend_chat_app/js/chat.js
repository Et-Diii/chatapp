import { apiData, escapeHtml, getCurrentUser, initials, nameOf, requireAuth, setupSharedUi, showMessage } from './api.js';
import { connectWebSocket, subscribeToMessages, sendMessage, disconnectWebSocket } from './websocket.js';

if (requireAuth()) { setupSharedUi(); void initChat(); }

async function initChat() {
  const params = new URLSearchParams(location.search);
  const conversationId = params.get('conversationId');
  const history = document.querySelector('[data-messages]');
  const error = document.querySelector('[data-chat-error]');
  const current = getCurrentUser() || {};
  if (!conversationId) { history.innerHTML = '<p class="empty-state">Đường dẫn cuộc trò chuyện đang thiếu mã.</p>'; return; }

  let contactOnline = false;
  let oldestPage = 0;
  try {
    const firstPage = await apiData(`/api/conversations/${encodeURIComponent(conversationId)}/messages?page=0&size=30`);
    let data = firstPage;
    if (!Array.isArray(firstPage) && firstPage?.totalPages > 1) {
      oldestPage = firstPage.totalPages - 1;
      data = await apiData(`/api/conversations/${encodeURIComponent(conversationId)}/messages?page=${oldestPage}&size=30`);
    }
    const messages = Array.isArray(data) ? data : data?.content || data?.items || data?.messages || [];
    messages.sort((a, b) => new Date(a.createdAt || a.timestamp || 0) - new Date(b.createdAt || b.timestamp || 0));
    history.replaceChildren();
    if (!messages.length) history.innerHTML = '<p class="empty-state">Hãy gửi lời chào để bắt đầu trò chuyện.</p>';
    else {
      messages.forEach(message => renderMessage(message));
      if (oldestPage > 0) addOlderMessagesButton();
    }
    history.scrollTop = history.scrollHeight;
  } catch (err) { history.innerHTML = `<p class="empty-state">${escapeHtml(err.message)}</p>`; }
  try { await apiData(`/api/conversations/${encodeURIComponent(conversationId)}/read`, { method: 'PUT' }); } catch { /* History remains visible if read receipts fail. */ }

  try {
    const conversations = await apiData('/api/conversations');
    const list = Array.isArray(conversations) ? conversations : conversations?.content || [];
    const conversation = list.find(item => String(item.id ?? item.conversationId) === String(conversationId));
    const person = conversation?.otherUser || conversation?.friend || conversation?.participant || conversation?.user;
    if (person) {
      contactOnline = String(person.status || '').toUpperCase() === 'ONLINE';
      document.querySelector('[data-chat-name]').textContent = nameOf(person);
      const avatar = document.querySelector('[data-chat-avatar]');
      const url = person.avatarUrl || person.avatar;
      avatar.src = url || `https://placehold.co/96x96/dff1ec/176b63?text=${encodeURIComponent(initials(nameOf(person)))}`;
      avatar.alt = `Ảnh đại diện của ${nameOf(person)}`;
      document.title = `${nameOf(person)} · Rinchat`;
      updateContactStatus();
    }
  } catch { /* Conversation list may be unavailable while messages still load. */ }

  connectWebSocket(() => {
    updateContactStatus();
    subscribeToMessages(message => {
      if (String(message.conversationId) === String(conversationId)) {
        if (message.eventType === 'READ') {
          const bubble = history.querySelector(`[data-message-id="${CSS.escape(String(message.id))}"]`);
          const status = bubble?.querySelector('.message-status');
          if (status) status.textContent = 'Đã xem';
        } else {
          renderMessage(message, conversationId);
          const senderId = message.senderId ?? message.sender?.id ?? message.sender?.userId;
          if (message.id && String(senderId) !== String(current.id ?? current.userId) && !message.isRead) {
            void apiData(`/api/messages/${encodeURIComponent(message.id)}/read`, { method: 'PUT' }).catch(() => {});
          }
        }
        history.scrollTop = history.scrollHeight;
      }
    });
  }, err => showMessage(error, err.message));

  const form = document.querySelector('[data-message-form]');
  const input = form.elements.content;
  input.addEventListener('input', () => { input.style.height = 'auto'; input.style.height = `${Math.min(input.scrollHeight, 120)}px`; });
  input.addEventListener('keydown', event => {
    if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); form.requestSubmit(); }
  });
  form.addEventListener('submit', event => {
    event.preventDefault();
    const content = input.value.trim();
    if (!content) return;
    try {
      sendMessage(conversationId, content);
      input.value = ''; input.style.height = 'auto'; showMessage(error, '');
    } catch (err) { showMessage(error, err.message); }
  });
  window.addEventListener('pagehide', disconnectWebSocket, { once: true });

  function updateContactStatus() {
    const status = document.querySelector('[data-chat-status]');
    if (status) status.innerHTML = `<i class="status-dot ${contactOnline ? '' : 'is-offline'}"></i> ${contactOnline ? 'Trực tuyến' : 'Ngoại tuyến'}`;
  }

  function addOlderMessagesButton() {
    if (history.querySelector('[data-load-older]')) return;
    const button = document.createElement('button');
    button.type = 'button';
    button.dataset.loadOlder = '';
    button.className = 'button button-quiet';
    button.textContent = 'Tải tin nhắn cũ hơn';
    button.addEventListener('click', async () => {
      if (oldestPage <= 0) return;
      button.disabled = true;
      button.textContent = 'Đang tải…';
      const previousHeight = history.scrollHeight;
      try {
        const data = await apiData(`/api/conversations/${encodeURIComponent(conversationId)}/messages?page=${oldestPage - 1}&size=30`);
        const older = Array.isArray(data) ? data : data?.content || data?.items || data?.messages || [];
        oldestPage -= 1;
        older.reverse().forEach(message => history.insertBefore(createMessageNode(message), history.querySelector('[data-message-id]')));
        history.scrollTop += history.scrollHeight - previousHeight;
        if (oldestPage === 0) button.remove();
        else { button.disabled = false; button.textContent = 'Tải tin nhắn cũ hơn'; }
      } catch (err) {
        button.disabled = false;
        button.textContent = 'Thử tải lại tin nhắn cũ';
        showMessage(error, err.message);
      }
    });
    history.prepend(button);
  }

  function renderMessage(message) {
    const empty = history.querySelector('.empty-state'); if (empty) empty.remove();
    history.append(createMessageNode(message));
  }

  function createMessageNode(message) {
    const senderId = message.senderId ?? message.sender?.id ?? message.sender?.userId ?? message.sender?.username;
    const mine = String(senderId) === String(current.id ?? current.userId ?? current.username);
    const content = message.content ?? message.message ?? '';
    const stamp = message.createdAt || message.timestamp || message.sentAt;
    let time = '';
    if (stamp) { const date = new Date(stamp); if (!Number.isNaN(date.getTime())) time = date.toLocaleTimeString('vi-VN', { hour: 'numeric', minute: '2-digit' }); }
    const node = document.createElement('article');
    node.dataset.messageId = message.id ?? '';
    node.className = `message ${mine ? 'mine' : 'theirs'}`;
    node.innerHTML = `<div class="message-bubble">${escapeHtml(content)}</div>${time ? `<time class="message-time">${escapeHtml(time)}</time>` : ''}${mine ? `<span class="message-status">${message.isRead ? 'Đã xem' : 'Đã gửi'}</span>` : ''}`;
    return node;
  }
}
