import { apiData, escapeHtml, requireAuth, setupSharedUi, showMessage } from './api.js';
import { renderPersonCard } from './ui.js';

if (requireAuth()) { setupSharedUi(); initSearch(); }

function initSearch() {
  const form = document.querySelector('[data-search-form]');
  const input = form.elements.keyword;
  const resultsEl = document.querySelector('[data-results]');
  const messageEl = document.querySelector('[data-search-message]');
  form.addEventListener('submit', async event => {
    event.preventDefault();
    const keyword = input.value.trim();
    if (!keyword) { resultsEl.innerHTML = ''; messageEl.textContent = 'Nhập tên hoặc tên tài khoản để tìm kiếm.'; return; }
    messageEl.textContent = 'Đang tìm bạn bè…';
    resultsEl.innerHTML = '<p class="loading-state">Đang tìm…</p>';
    try {
      const data = await apiData(`/api/users/search?keyword=${encodeURIComponent(keyword)}`);
      const users = Array.isArray(data) ? data : data?.content || data?.items || data?.data || [];
      messageEl.textContent = `Tìm thấy ${users.length} người dùng`;
      resultsEl.innerHTML = users.length ? users.map(user => renderPersonCard(user, relationshipAction(user))).join('') : '<p class="empty-state">Chưa tìm thấy kết quả. Thử tên khác nhé.</p>';
      bindActions(resultsEl, users);
    } catch (error) { messageEl.textContent = error.message; resultsEl.innerHTML = ''; }
  });
}

function relationshipAction(user) {
  const status = String(user.friendshipStatus || user.relationshipStatus || user.status || '').toUpperCase();
  if (user.isFriend || status === 'FRIENDS' || status === 'ACCEPTED') return '<button class="button button-outline" disabled>Bạn bè ✓</button>';
  if (user.requestSent || status === 'PENDING' || status === 'SENT') return '<button class="button button-outline" disabled>Đã gửi lời mời</button>';
  if (user.requestReceived || status === 'RECEIVED') return '<button class="button button-primary" data-accept>Chấp nhận lời mời</button>';
  return '<button class="button button-primary" data-add>Kết bạn</button>';
}

function bindActions(root, users) {
  root.querySelectorAll('.person-card').forEach((card, index) => {
    const button = card.querySelector('[data-add], [data-accept]');
    if (!button) return;
    button.addEventListener('click', async () => {
      button.disabled = true;
      try {
        const user = users[index];
        if (button.hasAttribute('data-accept')) {
          const requestId = user.friendRequestId || user.requestId;
          if (!requestId) throw new Error('Không tìm thấy lời mời. Hãy tải lại trang và thử lại.');
          await apiData(`/api/friend-requests/${encodeURIComponent(requestId)}/accept`, { method: 'PUT' });
          button.textContent = 'Bạn bè ✓';
        } else {
          await apiData(`/api/friend-requests/${encodeURIComponent(user.id ?? user.userId)}`, { method: 'POST' });
          button.textContent = 'Đã gửi lời mời';
        }
        button.disabled = true;
        button.className = 'button button-outline';
      } catch (error) { button.disabled = false; button.textContent = error.message; }
    });
  });
}
