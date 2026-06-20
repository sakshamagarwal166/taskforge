package com.taskforge.task.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MoveTaskRequest(

        @NotNull(message = "Column ID is required")
        UUID columnId,

        @NotNull(message = "Position is required")
        Integer position
) {
}
