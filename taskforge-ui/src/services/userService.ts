import api from './api';
import type { Page, User, Role } from '../types';

export async function getUsers(): Promise<Page<User>> {
  const { data } = await api.get<Page<User>>('/users', { params: { size: 100 } });
  return data;
}

export async function createUser(
  email: string,
  password: string,
  firstName: string,
  lastName: string,
  role: Role
): Promise<User> {
  const { data } = await api.post<User>('/users', {
    email, password, firstName, lastName, role,
  });
  return data;
}
