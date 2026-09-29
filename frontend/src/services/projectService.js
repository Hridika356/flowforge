import { apiRequest } from './api';

export function getProjects({ status, q } = {}) {
  const params = new URLSearchParams();
  if (status) params.set('status', status);
  if (q) params.set('q', q);
  const qs = params.toString();
  return apiRequest(`/api/projects${qs ? `?${qs}` : ''}`);
}

export const getProject = (id) => apiRequest(`/api/projects/${id}`);

export const createProject = (data) => apiRequest('/api/projects', { method: 'POST', body: data });

export const updateProject = (id, data) => apiRequest(`/api/projects/${id}`, { method: 'PUT', body: data });

export const deleteProject = (id) => apiRequest(`/api/projects/${id}`, { method: 'DELETE' });

export const getDashboard = () => apiRequest('/api/dashboard');
