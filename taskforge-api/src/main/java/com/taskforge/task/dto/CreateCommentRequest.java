package com.taskforge.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCommentRequest(

        @NotBlank(message = "Content is required")
        String content,

        @NotNull(message = "Author ID is required")
        UUID authorId
) {
}
