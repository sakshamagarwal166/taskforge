package com.taskforge.task.dto;

import com.taskforge.task.Priority;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskRequest(

        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        String description,

        Priority priority,

        UUID assigneeId,

        LocalDate dueDate
) {
}
