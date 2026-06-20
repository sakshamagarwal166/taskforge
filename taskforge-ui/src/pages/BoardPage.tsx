import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import type { DropResult } from '@hello-pangea/dnd';
import { ArrowLeft, Plus, Flag } from 'lucide-react';
import { getProjectById } from '../services/projectService';
import { getTasks, createTask, moveTask } from '../services/taskService';
import { getUsers } from '../services/userService';
import Modal from '../components/Modal';
import TaskPanel from '../components/TaskPanel';
import type { Project, Task, User, BoardColumn } from '../types';

const priorityConfig: Record<string, { label: string; color: string; dot: string }> = {
  URGENT: { label: 'Urgent', color: 'bg-red-100 text-red-700', dot: 'bg-red-500' },
  HIGH: { label: 'High', color: 'bg-orange-100 text-orange-700', dot: 'bg-orange-500' },
  MEDIUM: { label: 'Medium', color: 'bg-blue-100 text-blue-700', dot: 'bg-blue-500' },
  LOW: { label: 'Low', color: 'bg-gray-100 text-gray-600', dot: 'bg-gray-400' },
};

export default function BoardPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const [project, setProject] = useState<Project | null>(null);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [addingToColumn, setAddingToColumn] = useState<BoardColumn | null>(null);
  const [newTaskForm, setNewTaskForm] = useState({ title: '', description: '', priority: 'MEDIUM' });
  const [loading, setLoading] = useState(false);

  const loadData = useCallback(async () => {
    if (!projectId) return;
    const [proj, taskList, userList] = await Promise.all([
      getProjectById(projectId),
      getTasks(projectId),
      getUsers(),
    ]);
    setProject(proj);
    setTasks(taskList);
    setUsers(userList.content);
  }, [projectId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  function getTasksForColumn(columnId: string): Task[] {
    return tasks
      .filter((t) => t.columnId === columnId)
      .sort((a, b) => a.position - b.position);
  }

  function getUserInitials(userId: string | null): string {
    if (!userId) return '?';
    const user = users.find((u) => u.id === userId);
    return user ? `${user.firstName[0]}${user.lastName[0]}` : '?';
  }

  async function handleDragEnd(result: DropResult) {
    if (!result.destination || !projectId) return;

    const taskId = result.draggableId;
    const newColumnId = result.destination.droppableId;
    const newPosition = result.destination.index;

    const task = tasks.find((t) => t.id === taskId);
    if (!task) return;

    setTasks((prev) =>
      prev.map((t) => (t.id === taskId ? { ...t, columnId: newColumnId, position: newPosition } : t))
    );

    try {
      await moveTask(projectId, taskId, newColumnId, newPosition);
    } catch {
      loadData();
    }
  }

  async function handleCreateTask(e: React.FormEvent) {
    e.preventDefault();
    if (!projectId) return;
    setLoading(true);
    try {
      const task = await createTask(projectId, newTaskForm.title, newTaskForm.description, newTaskForm.priority);
      setTasks([...tasks, task]);
      setAddingToColumn(null);
      setNewTaskForm({ title: '', description: '', priority: 'MEDIUM' });
    } catch { /* handled by interceptor */ }
    setLoading(false);
  }

  if (!project) {
    return (
      <div className="px-8 py-8">
        <p className="text-gray-400 text-sm">Loading project...</p>
      </div>
    );
  }

  const sortedColumns = [...project.columns].sort((a, b) => a.position - b.position);

  return (
    <div className="h-screen flex flex-col overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-200 flex items-center gap-4 shrink-0">
        <Link to="/projects" className="text-gray-400 hover:text-gray-600">
          <ArrowLeft size={20} />
        </Link>
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-mono bg-gray-100 text-gray-500 px-2 py-0.5 rounded">
              {project.projectKey}
            </span>
            <h1 className="text-lg font-semibold text-gray-900">{project.name}</h1>
          </div>
        </div>
      </div>

      <DragDropContext onDragEnd={handleDragEnd}>
        <div className="flex-1 flex gap-4 p-6 overflow-x-auto">
          {sortedColumns.map((column) => (
            <div key={column.id} className="w-72 shrink-0 flex flex-col">
              <div className="flex items-center justify-between mb-3 px-1">
                <div className="flex items-center gap-2">
                  <div className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: column.color }} />
                  <h3 className="text-sm font-medium text-gray-700">{column.name}</h3>
                  <span className="text-xs text-gray-400">{getTasksForColumn(column.id).length}</span>
                </div>
              </div>

              <Droppable droppableId={column.id}>
                {(provided, snapshot) => (
                  <div
                    ref={provided.innerRef}
                    {...provided.droppableProps}
                    className={`flex-1 space-y-2 rounded-lg p-2 min-h-[200px] transition-colors ${
                      snapshot.isDraggingOver ? 'bg-gray-100' : 'bg-gray-50'
                    }`}
                  >
                    {getTasksForColumn(column.id).map((task, index) => (
                      <Draggable key={task.id} draggableId={task.id} index={index}>
                        {(provided, snapshot) => (
                          <div
                            ref={provided.innerRef}
                            {...provided.draggableProps}
                            {...provided.dragHandleProps}
                            onClick={() => setSelectedTask(task)}
                            className={`bg-white border border-gray-200 rounded-lg p-3 cursor-pointer hover:border-gray-300 transition-shadow ${
                              snapshot.isDragging ? 'shadow-lg' : 'shadow-sm'
                            }`}
                          >
                            <p className="text-sm text-gray-900 font-medium mb-2">{task.title}</p>
                            <div className="flex items-center justify-between">
                              <span className={`inline-flex items-center gap-1 text-xs px-1.5 py-0.5 rounded ${
                                priorityConfig[task.priority]?.color ?? 'bg-gray-100 text-gray-600'
                              }`}>
                                <Flag size={10} />
                                {priorityConfig[task.priority]?.label ?? task.priority}
                              </span>
                              {task.assigneeId && (
                                <div className="w-6 h-6 rounded-full bg-gray-200 flex items-center justify-center text-[10px] font-medium text-gray-600">
                                  {getUserInitials(task.assigneeId)}
                                </div>
                              )}
                            </div>
                          </div>
                        )}
                      </Draggable>
                    ))}
                    {provided.placeholder}
                  </div>
                )}
              </Droppable>

              <button
                onClick={() => setAddingToColumn(column)}
                className="mt-2 flex items-center gap-1 text-sm text-gray-400 hover:text-gray-600 px-2 py-1.5 rounded-md hover:bg-gray-100 transition-colors"
              >
                <Plus size={14} />
                Add task
              </button>
            </div>
          ))}
        </div>
      </DragDropContext>

      {selectedTask && (
        <TaskPanel
          task={selectedTask}
          users={users}
          onClose={() => setSelectedTask(null)}
        />
      )}

      {addingToColumn && (
        <Modal title={`Add task to ${addingToColumn.name}`} onClose={() => setAddingToColumn(null)}>
          <form onSubmit={handleCreateTask} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Title</label>
              <input
                type="text"
                value={newTaskForm.title}
                onChange={(e) => setNewTaskForm({ ...newTaskForm, title: e.target.value })}
                placeholder="Task title"
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
              <textarea
                value={newTaskForm.description}
                onChange={(e) => setNewTaskForm({ ...newTaskForm, description: e.target.value })}
                placeholder="Optional description"
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent resize-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Priority</label>
              <select
                value={newTaskForm.priority}
                onChange={(e) => setNewTaskForm({ ...newTaskForm, priority: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
              </select>
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full py-2 bg-gray-900 text-white text-sm font-medium rounded-md hover:bg-gray-800 disabled:opacity-50 transition-colors"
            >
              {loading ? 'Creating...' : 'Create Task'}
            </button>
          </form>
        </Modal>
      )}
    </div>
  );
}
