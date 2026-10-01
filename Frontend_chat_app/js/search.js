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
    if (!keyword) { resultsEl.innerHTML = ''; messageEl.textContent = 'Type a name or username to search.'; return; }
    messageEl.textContent = 'Looking for people…';
    resultsEl.innerHTML = '<p class="loading-state">Searching…</p>';
    try {
      const data = await apiData(`/api/users/search?keyword=${encodeURIComponent(keyword)}`);
      const users = Array.isArray(data) ? data : data?.content || data?.items || data?.data || [];
      messageEl.textContent = `${users.length} ${users.length === 1 ? 'person' : 'people'} found`;
      resultsEl.innerHTML = users.length ? users.map(user => renderPersonCard(user, relationshipAction(user))).join('') : '<p class="empty-state">No matches yet. Try another name.</p>';
      bindActions(resultsEl, users);
    } catch (error) { messageEl.textContent = error.message; resultsEl.innerHTML = ''; }
  });
}

function relationshipAction(user) {
  const status = String(user.friendshipStatus || user.relationshipStatus || user.status || '').toUpperCase();
  if (user.isFriend || status === 'FRIENDS' || status === 'ACCEPTED') return '<button class="button button-outline" disabled>Friends ✓</button>';
  if (user.requestSent || status === 'PENDING' || status === 'SENT') return '<button class="button button-outline" disabled>Request sent</button>';
  if (user.requestReceived || status === 'RECEIVED') return '<button class="button button-primary" data-accept>Accept request</button>';
  return '<button class="button button-primary" data-add>Add friend</button>';
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
          if (!requestId) throw new Error('Could not find the request ID. Refresh the page and try again.');
          await apiData(`/api/friend-requests/${encodeURIComponent(requestId)}/accept`, { method: 'PUT' });
          button.textContent = 'Friends ✓';
        } else {
          await apiData(`/api/friend-requests/${encodeURIComponent(user.id ?? user.userId)}`, { method: 'POST' });
          button.textContent = 'Request sent';
        }
        button.disabled = true;
        button.className = 'button button-outline';
      } catch (error) { button.disabled = false; button.textContent = error.message; }
    });
  });
}
