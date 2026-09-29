import { apiRequest } from './api';

export const getProjectTasks = (projectId) => apiRequest(`/api/projects/${projectId}/tasks`);

export const createTask = (projectId, data) =>
  apiRequest(`/api/projects/${projectId}/tasks`, { method: 'POST', body: data });

export const updateTask = (taskId, data) => apiRequest(`/api/tasks/${taskId}`, { method: 'PUT', body: data });

export const updateTaskStatus = (taskId, status) =>
  apiRequest(`/api/tasks/${taskId}/status`, { method: 'PATCH', body: { status } });

export const deleteTask = (taskId) => apiRequest(`/api/tasks/${taskId}`, { method: 'DELETE' });
