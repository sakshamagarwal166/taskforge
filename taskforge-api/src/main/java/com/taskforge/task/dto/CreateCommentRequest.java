package com.taskforge.task.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(

        @NotBlank(message = "Content is required")
        String content
) {
}
