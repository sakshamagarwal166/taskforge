import api from './api';
import type { Page, AuditLog } from '../types';

export async function getAuditLogs(page = 0, size = 20): Promise<Page<AuditLog>> {
  const { data } = await api.get<Page<AuditLog>>('/audit', { params: { page, size } });
  return data;
}
