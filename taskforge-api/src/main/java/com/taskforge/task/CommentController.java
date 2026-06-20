package com.taskforge.task;

import com.taskforge.task.dto.CommentResponse;
import com.taskforge.task.dto.CreateCommentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Task comment APIs")
public class CommentController {

    private final TaskCommentService commentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER')")
    @Operation(summary = "Add a comment to a task")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(taskId, request));
    }

    @GetMapping
    @Operation(summary = "List comments on a task")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID taskId) {
        return ResponseEntity.ok(commentService.getCommentsByTask(taskId));
    }
}
