import api from './api';
import type { Page, Project } from '../types';

export async function getProjects(): Promise<Page<Project>> {
  const { data } = await api.get<Page<Project>>('/projects', { params: { size: 50 } });
  return data;
}

export async function getProjectById(projectId: string): Promise<Project> {
  const { data } = await api.get<Project>(`/projects/${projectId}`);
  return data;
}

export async function createProject(name: string, description: string): Promise<Project> {
  const { data } = await api.post<Project>('/projects', { name, description });
  return data;
}
