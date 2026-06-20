import api from './api';
import type { Task, Comment } from '../types';

export async function getTasks(projectId: string): Promise<Task[]> {
  const { data } = await api.get<Task[]>(`/projects/${projectId}/tasks`);
  return data;
}

export async function createTask(
  projectId: string,
  title: string,
  description: string,
  priority: string,
  assigneeId?: string
): Promise<Task> {
  const { data } = await api.post<Task>(`/projects/${projectId}/tasks`, {
    title, description, priority, assigneeId: assigneeId || null,
  });
  return data;
}

export async function updateTask(
  projectId: string,
  taskId: string,
  updates: Record<string, unknown>
): Promise<Task> {
  const { data } = await api.put<Task>(`/projects/${projectId}/tasks/${taskId}`, updates);
  return data;
}

export async function moveTask(
  projectId: string,
  taskId: string,
  columnId: string,
  position: number
): Promise<Task> {
  const { data } = await api.patch<Task>(`/projects/${projectId}/tasks/${taskId}/move`, {
    columnId, position,
  });
  return data;
}

export async function getComments(taskId: string): Promise<Comment[]> {
  const { data } = await api.get<Comment[]>(`/tasks/${taskId}/comments`);
  return data;
}

export async function addComment(taskId: string, content: string): Promise<Comment> {
  const { data } = await api.post<Comment>(`/tasks/${taskId}/comments`, { content });
  return data;
}
