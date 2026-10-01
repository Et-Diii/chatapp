import { apiData, getToken } from './api.js';

export function redirectIfSignedIn() {
  if (getToken()) location.replace('/index.html');
}

export async function submitLogin(identifier, password) {
  const result = await apiData('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ identifier, password })
  });
  const token = result?.token || result?.accessToken || result?.jwt;
  if (!token) throw new Error('Máy chủ không trả về mã đăng nhập.');
  localStorage.setItem('token', token);
  const user = result.user || result.profile || { username: identifier };
  localStorage.setItem('user', JSON.stringify(user));
  return user;
}

export function validateEmail(email) { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email); }
