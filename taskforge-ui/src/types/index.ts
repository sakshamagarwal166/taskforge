export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
  tenantId: string;
  tenantSlug: string;
}

export type Role = 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER';

export interface User {
  id: string;
  tenantId: string;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
  active: boolean;
  createdAt: string;
}

export interface Project {
  id: string;
  tenantId: string;
  name: string;
  description: string;
  projectKey: string;
  ownerId: string;
  status: 'ACTIVE' | 'ARCHIVED';
  columns: BoardColumn[];
  createdAt: string;
}

export interface BoardColumn {
  id: string;
  name: string;
  position: number;
  color: string;
}

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface Task {
  id: string;
  projectId: string;
  columnId: string;
  taskNumber: number;
  title: string;
  description: string;
  priority: Priority;
  assigneeId: string | null;
  reporterId: string;
  position: number;
  dueDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Comment {
  id: string;
  taskId: string;
  authorId: string;
  content: string;
  createdAt: string;
}

export interface AuditLog {
  id: string;
  userId: string;
  action: string;
  entityType: string;
  entityId: string;
  oldValue: Record<string, unknown> | null;
  newValue: Record<string, unknown> | null;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
