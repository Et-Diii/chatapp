import { apiData, endpointList, escapeHtml, avatarMarkup, nameOf, requireAuth, setupSharedUi, showMessage } from './api.js';
import { renderPersonCard, renderPersonRow, loadFriends, startConversation, personFromRequest } from './ui.js';

if (requireAuth()) { setupSharedUi(); void loadPage(); }

async function loadPage() {
  const friendsEl = document.querySelector('[data-friends]');
  try {
    const friends = await loadFriends();
    document.querySelector('[data-friend-count]').textContent = String(friends.length);
    friendsEl.innerHTML = friends.length ? friends.map(friend => renderPersonCard(friend, '<button class="button button-primary" data-chat>Message →</button><button class="button button-outline" data-remove>Remove</button>')).join('') : '<p class="empty-state">Your circle starts with a hello. Find someone to connect with.</p>';
    friendsEl.querySelectorAll('[data-chat]').forEach((button, index) => button.addEventListener('click', async () => {
      button.disabled = true;
      try { await startConversation(friends[index]); }
      catch (error) { button.disabled = false; button.textContent = error.message; }
    }));
    friendsEl.querySelectorAll('[data-remove]').forEach((button, index) => button.addEventListener('click', async () => {
      button.disabled = true;
      try { await apiData(`/api/friends/${encodeURIComponent(friends[index].id ?? friends[index].userId)}`, { method: 'DELETE' }); button.closest('.person-card').remove(); document.querySelector('[data-friend-count]').textContent = String(Math.max(0, Number(document.querySelector('[data-friend-count]').textContent)-1)); }
      catch (error) { button.disabled = false; button.textContent = error.message; }
    }));
  } catch (error) { friendsEl.innerHTML = `<p class="empty-state">${escapeHtml(error.message)}</p>`; }

  await loadRequests('received', '/api/friend-requests/received', '/api/friend-requests?status=RECEIVED');
  await loadRequests('sent', '/api/friend-requests/sent', '/api/friend-requests?status=SENT');
}

async function loadRequests(kind, ...urls) {
  const element = document.querySelector(`[data-${kind}-requests]`);
  try {
    const requests = await endpointList(...urls);
    if (kind === 'received') document.querySelector('[data-received-count]').textContent = String(requests.length);
    if (!requests.length) { element.innerHTML = '<p class="muted small">Nothing here right now.</p>'; return; }
    element.innerHTML = requests.map(request => {
      const user = personFromRequest(request, kind);
      if (kind === 'sent') return renderPersonRow(user, '<span class="muted small">Request sent</span><button class="button button-outline" data-cancel>Cancel</button>');
      return renderPersonRow(user, '<button class="button button-primary" data-accept>Accept</button><button class="button button-danger" data-reject>Reject</button>');
    }).join('');
    if (kind === 'received') element.querySelectorAll('.person-row').forEach((row, index) => {
      row.querySelector('[data-accept]').addEventListener('click', () => updateRequest(row, requests[index], 'accept'));
      row.querySelector('[data-reject]').addEventListener('click', () => updateRequest(row, requests[index], 'reject'));
    });
    if (kind === 'sent') element.querySelectorAll('.person-row').forEach((row, index) => row.querySelector('[data-cancel]').addEventListener('click', async () => {
      const button=row.querySelector('[data-cancel]'); button.disabled=true;
      try { await apiData(`/api/friend-requests/${encodeURIComponent(requests[index].id)}`,{method:'DELETE'}); row.remove(); }
      catch(error) { button.disabled=false; button.textContent=error.message; }
    }));
  } catch (error) { element.innerHTML = `<p class="muted small">${escapeHtml(error.message)}</p>`; }
}

async function updateRequest(row, request, action) {
  const id = request.id ?? request.requestId;
  row.querySelectorAll('button').forEach(button => { button.disabled = true; });
  try {
    await apiData(`/api/friend-requests/${encodeURIComponent(id)}/${action}`, { method: 'PUT' });
    row.remove();
    const count = document.querySelector('[data-received-count]');
    count.textContent = String(Math.max(0, Number(count.textContent) - 1));
  } catch (error) {
    row.querySelectorAll('button').forEach(button => { button.disabled = false; });
    const status = document.createElement('span'); status.className = 'muted small'; status.textContent = error.message; row.append(status);
  }
}
