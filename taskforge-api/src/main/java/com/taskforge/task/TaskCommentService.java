package com.taskforge.task;

import com.taskforge.audit.Auditable;
import com.taskforge.auth.SecurityUtils;
import com.taskforge.common.exception.ResourceNotFoundException;
import com.taskforge.multitenancy.TenantContext;
import com.taskforge.task.dto.CommentResponse;
import com.taskforge.task.dto.CreateCommentRequest;
import com.taskforge.user.User;
import com.taskforge.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    @Auditable(action = "CREATE", entity = "Comment")
    public CommentResponse addComment(UUID taskId, CreateCommentRequest request) {
        Task task = findTask(taskId);

        UUID authorId = SecurityUtils.getCurrentUser().id();
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authorId));

        TaskComment comment = new TaskComment();
        comment.setTenant(task.getTenant());
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setContent(request.content());

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTask(UUID taskId) {
        findTask(taskId);
        return commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    private Task findTask(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        UUID tenantId = TenantContext.requireCurrentTenant();
        if (!task.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return task;
    }
}
