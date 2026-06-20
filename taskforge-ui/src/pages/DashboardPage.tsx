import { useState, useEffect } from 'react';
import { FolderKanban, CheckSquare, Clock } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { getProjects } from '../services/projectService';
import { getAuditLogs } from '../services/auditService';
import type { AuditLog } from '../types';

export default function DashboardPage() {
  const { user } = useAuth();
  const [projectCount, setProjectCount] = useState(0);
  const [recentActivity, setRecentActivity] = useState<AuditLog[]>([]);

  useEffect(() => {
    getProjects().then((data) => setProjectCount(data.totalElements)).catch(() => {});
    if (user?.role === 'OWNER' || user?.role === 'ADMIN') {
      getAuditLogs(0, 5).then((data) => setRecentActivity(data.content)).catch(() => {});
    }
  }, [user?.role]);

  const stats = [
    { label: 'Projects', value: projectCount, icon: FolderKanban },
    { label: 'Your Role', value: user?.role ?? '-', icon: CheckSquare },
    { label: 'Workspace', value: user?.tenantSlug ?? '-', icon: Clock },
  ];

  return (
    <div className="px-8 py-8 max-w-5xl">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">
          Welcome back, {user?.firstName}
        </h1>
        <p className="text-sm text-gray-500 mt-1">Here's what's happening in your workspace.</p>
      </div>

      <div className="grid grid-cols-3 gap-4 mb-8">
        {stats.map((stat) => (
          <div key={stat.label} className="border border-gray-200 rounded-lg p-5">
            <div className="flex items-center gap-3 mb-2">
              <stat.icon size={18} className="text-gray-400" />
              <span className="text-sm text-gray-500">{stat.label}</span>
            </div>
            <p className="text-2xl font-semibold text-gray-900">{stat.value}</p>
          </div>
        ))}
      </div>

      {recentActivity.length > 0 && (
        <div>
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Recent Activity</h2>
          <div className="border border-gray-200 rounded-lg divide-y divide-gray-100">
            {recentActivity.map((log) => (
              <div key={log.id} className="px-4 py-3 flex items-center justify-between">
                <div>
                  <span className="text-sm text-gray-900 font-medium">{log.action}</span>
                  <span className="text-sm text-gray-500"> on </span>
                  <span className="text-sm text-gray-700">{log.entityType}</span>
                </div>
                <span className="text-xs text-gray-400">
                  {new Date(log.createdAt).toLocaleDateString()}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
