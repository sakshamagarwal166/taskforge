package com.taskforge.task;

import com.taskforge.audit.AuditContext;
import com.taskforge.audit.Auditable;
import com.taskforge.auth.SecurityUtils;
import com.taskforge.common.exception.BadRequestException;
import com.taskforge.common.exception.ResourceNotFoundException;
import com.taskforge.multitenancy.TenantContext;
import com.taskforge.project.BoardColumn;
import com.taskforge.project.BoardColumnRepository;
import com.taskforge.project.Project;
import com.taskforge.project.ProjectRepository;
import com.taskforge.task.dto.CreateTaskRequest;
import com.taskforge.task.dto.MoveTaskRequest;
import com.taskforge.task.dto.TaskResponse;
import com.taskforge.task.dto.UpdateTaskRequest;
import com.taskforge.user.User;
import com.taskforge.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final UserRepository userRepository;

    @Transactional
    @Auditable(action = "CREATE", entity = "Task")
    public TaskResponse createTask(UUID projectId, CreateTaskRequest request) {
        Project project = findProject(projectId);
        User reporter = findUser(SecurityUtils.getCurrentUser().id());
        User assignee = request.assigneeId() != null ? findUser(request.assigneeId()) : null;
        BoardColumn firstColumn = getFirstColumn(project);

        Task task = new Task();
        task.setTenant(project.getTenant());
        task.setProject(project);
        task.setColumn(firstColumn);
        task.setTaskNumber(taskRepository.findMaxTaskNumber(projectId) + 1);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority() != null ? request.priority() : Priority.MEDIUM);
        task.setAssignee(assignee);
        task.setReporter(reporter);
        task.setPosition(0);
        task.setDueDate(request.dueDate());

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    @Auditable(action = "UPDATE", entity = "Task")
    public TaskResponse updateTask(UUID projectId, UUID taskId, UpdateTaskRequest request) {
        Task task = findTaskInProject(projectId, taskId);
        AuditContext.setOldValue(TaskResponse.from(task));

        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        if (request.assigneeId() != null) task.setAssignee(findUser(request.assigneeId()));

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    @Auditable(action = "MOVE", entity = "Task")
    public TaskResponse moveTask(UUID projectId, UUID taskId, MoveTaskRequest request) {
        Task task = findTaskInProject(projectId, taskId);
        AuditContext.setOldValue(TaskResponse.from(task));

        BoardColumn targetColumn = boardColumnRepository.findById(request.columnId())
                .orElseThrow(() -> new ResourceNotFoundException("Column not found: " + request.columnId()));
        if (!targetColumn.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Column does not belong to this project");
        }

        task.setColumn(targetColumn);
        task.setPosition(request.position());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(UUID projectId) {
        findProject(projectId);
        return taskRepository.findAllByProjectIdOrderByPositionAsc(projectId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    private Project findProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        UUID tenantId = TenantContext.requireCurrentTenant();
        if (!project.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
        return project;
    }

    private Task findTaskInProject(UUID projectId, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        UUID tenantId = TenantContext.requireCurrentTenant();
        if (!task.getTenant().getId().equals(tenantId) || !task.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return task;
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private BoardColumn getFirstColumn(Project project) {
        return project.getColumns().stream()
                .filter(col -> col.getPosition() == 0)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Project has no columns configured"));
    }
}
