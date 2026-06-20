package com.taskforge.task.dto;

import com.taskforge.task.Priority;
import com.taskforge.task.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID projectId,
        UUID columnId,
        int taskNumber,
        String title,
        String description,
        Priority priority,
        UUID assigneeId,
        UUID reporterId,
        int position,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getColumn().getId(),
                task.getTaskNumber(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getReporter().getId(),
                task.getPosition(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
