import { useState, useEffect } from 'react';
import { X, Send, Calendar, Flag } from 'lucide-react';
import type { Task, Comment, User } from '../types';
import { getComments, addComment } from '../services/taskService';

const priorityConfig: Record<string, { label: string; color: string }> = {
  URGENT: { label: 'Urgent', color: 'bg-red-100 text-red-700' },
  HIGH: { label: 'High', color: 'bg-orange-100 text-orange-700' },
  MEDIUM: { label: 'Medium', color: 'bg-blue-100 text-blue-700' },
  LOW: { label: 'Low', color: 'bg-gray-100 text-gray-600' },
};

interface TaskPanelProps {
  task: Task;
  users: User[];
  onClose: () => void;
}

export default function TaskPanel({ task, users, onClose }: TaskPanelProps) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    getComments(task.id).then(setComments).catch(() => {});
  }, [task.id]);

  function getUserName(userId: string): string {
    const user = users.find((u) => u.id === userId);
    return user ? `${user.firstName} ${user.lastName}` : 'Unknown';
  }

  async function handleAddComment(e: React.FormEvent) {
    e.preventDefault();
    if (!newComment.trim()) return;
    setLoading(true);
    try {
      const comment = await addComment(task.id, newComment.trim());
      setComments([...comments, comment]);
      setNewComment('');
    } catch { /* handled by interceptor */ }
    setLoading(false);
  }

  const priority = priorityConfig[task.priority] ?? priorityConfig.MEDIUM;

  return (
    <div className="fixed inset-y-0 right-0 w-full max-w-md bg-white border-l border-gray-200 shadow-xl z-40 flex flex-col">
      <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
        <h2 className="text-lg font-semibold text-gray-900 truncate pr-4">{task.title}</h2>
        <button onClick={onClose} className="text-gray-400 hover:text-gray-600 shrink-0">
          <X size={20} />
        </button>
      </div>

      <div className="flex-1 overflow-y-auto px-6 py-5 space-y-6">
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-gray-500 mb-1">Priority</p>
            <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium ${priority.color}`}>
              <Flag size={12} />
              {priority.label}
            </span>
          </div>
          <div>
            <p className="text-gray-500 mb-1">Assignee</p>
            <p className="text-gray-900">{task.assigneeId ? getUserName(task.assigneeId) : 'Unassigned'}</p>
          </div>
          <div>
            <p className="text-gray-500 mb-1">Reporter</p>
            <p className="text-gray-900">{getUserName(task.reporterId)}</p>
          </div>
          {task.dueDate && (
            <div>
              <p className="text-gray-500 mb-1">Due Date</p>
              <p className="text-gray-900 flex items-center gap-1">
                <Calendar size={14} />
                {task.dueDate}
              </p>
            </div>
          )}
        </div>

        {task.description && (
          <div>
            <p className="text-sm text-gray-500 mb-1">Description</p>
            <p className="text-sm text-gray-800 whitespace-pre-wrap">{task.description}</p>
          </div>
        )}

        <div>
          <p className="text-sm font-medium text-gray-900 mb-3">
            Comments ({comments.length})
          </p>
          <div className="space-y-3">
            {comments.map((comment) => (
              <div key={comment.id} className="bg-gray-50 rounded-lg p-3">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-xs font-medium text-gray-700">
                    {getUserName(comment.authorId)}
                  </span>
                  <span className="text-xs text-gray-400">
                    {new Date(comment.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <p className="text-sm text-gray-800">{comment.content}</p>
              </div>
            ))}
            {comments.length === 0 && (
              <p className="text-sm text-gray-400">No comments yet.</p>
            )}
          </div>
        </div>
      </div>

      <form onSubmit={handleAddComment} className="px-6 py-4 border-t border-gray-200">
        <div className="flex gap-2">
          <input
            type="text"
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="Write a comment..."
            className="flex-1 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
          />
          <button
            type="submit"
            disabled={loading || !newComment.trim()}
            className="px-3 py-2 bg-gray-900 text-white rounded-md hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Send size={16} />
          </button>
        </div>
      </form>
    </div>
  );
}
