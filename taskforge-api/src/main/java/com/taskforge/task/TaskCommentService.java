package com.taskforge.task;

import com.taskforge.common.exception.ResourceNotFoundException;
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
    public CommentResponse addComment(UUID tenantId, UUID taskId, CreateCommentRequest request) {
        Task task = findTaskInTenant(tenantId, taskId);
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.authorId()));

        TaskComment comment = new TaskComment();
        comment.setTenant(task.getTenant());
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setContent(request.content());

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTask(UUID tenantId, UUID taskId) {
        findTaskInTenant(tenantId, taskId);
        return commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    private Task findTaskInTenant(UUID tenantId, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        if (!task.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return task;
    }
}
