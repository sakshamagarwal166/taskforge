package com.taskforge.task.dto;

import com.taskforge.task.TaskComment;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID taskId,
        UUID authorId,
        String content,
        LocalDateTime createdAt
) {
    public static CommentResponse from(TaskComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getAuthor().getId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
