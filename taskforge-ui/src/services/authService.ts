import api from './api';
import type { AuthResponse } from '../types';

export async function login(tenantSlug: string, email: string, password: string): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/login', { tenantSlug, email, password });
  storeAuth(data);
  return data;
}

export async function register(
  tenantSlug: string,
  tenantName: string,
  email: string,
  password: string,
  firstName: string,
  lastName: string
): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/register', {
    tenantSlug, tenantName, email, password, firstName, lastName,
  });
  storeAuth(data);
  return data;
}

export async function refreshToken(): Promise<AuthResponse> {
  const token = localStorage.getItem('refreshToken');
  const { data } = await api.post<AuthResponse>('/auth/refresh', { refreshToken: token });
  storeAuth(data);
  return data;
}

export function logout() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
  window.location.href = '/login';
}

function storeAuth(data: AuthResponse) {
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  localStorage.setItem('user', JSON.stringify({
    id: data.userId,
    email: data.email,
    firstName: data.firstName,
    lastName: data.lastName,
    role: data.role,
    tenantId: data.tenantId,
    tenantSlug: data.tenantSlug,
  }));
}
