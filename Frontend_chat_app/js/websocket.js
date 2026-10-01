import { API_BASE_URL, getToken } from './api.js';

let client;
const subscriptions = new Map();
const connectedCallbacks = new Set();
const errorCallbacks = new Set();

export function connectWebSocket(onConnected, onError) {
  if (onConnected) connectedCallbacks.add(onConnected);
  if (onError) errorCallbacks.add(onError);
  if (client?.active) {
    if (client.connected && onConnected) queueMicrotask(() => onConnected(client));
    return client;
  }
  const StompClient = window.StompJs?.Client;
  if (!StompClient) {
    const error = new Error('The messaging connection library did not load. Refresh and try again.');
    errorCallbacks.forEach(callback => callback(error));
    return null;
  }
  const backend = new URL(API_BASE_URL || location.origin);
  const protocol = backend.protocol === 'https:' ? 'wss:' : 'ws:';
  client = new StompClient({
    brokerURL: `${protocol}//${backend.host}/ws`,
    connectHeaders: { Authorization: `Bearer ${getToken() || ''}` },
    reconnectDelay: 4000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => { connectedCallbacks.forEach(callback => callback(client)); },
    onStompError: frame => errorCallbacks.forEach(callback => callback(new Error(frame.headers.message || 'Messaging service returned an error.'))),
    onWebSocketError: () => errorCallbacks.forEach(callback => callback(new Error('Could not connect to the messaging service.')))
  });
  client.activate();
  return client;
}

export function subscribeToMessages(callback) {
  if (!client?.connected) return null;
  const destination = '/user/queue/messages';
  if (subscriptions.has(destination)) return subscriptions.get(destination);
  const subscription = client.subscribe(destination, frame => {
    try { callback(JSON.parse(frame.body)); } catch { /* Ignore malformed frames. */ }
  });
  subscriptions.set(destination, subscription);
  return subscription;
}

export function sendMessage(conversationId, content) {
  if (!client?.connected) throw new Error('Connecting… please try sending again in a moment.');
  client.publish({ destination: '/app/chat', body: JSON.stringify({ conversationId, content }) });
}

export function disconnectWebSocket() {
  subscriptions.clear();
  connectedCallbacks.clear(); errorCallbacks.clear();
  if (client) { const old=client; client = undefined; void old.deactivate(); }
}
