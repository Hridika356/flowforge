import { apiRequest } from './api';

export const register = (data) => apiRequest('/api/auth/register', { method: 'POST', body: data, auth: false });

export const login = (data) => apiRequest('/api/auth/login', { method: 'POST', body: data, auth: false });

export const getCurrentUser = () => apiRequest('/api/users/me');
