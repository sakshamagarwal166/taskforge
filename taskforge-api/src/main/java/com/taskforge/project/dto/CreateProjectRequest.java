package com.taskforge.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(

        @NotBlank(message = "Project name is required")
        @Size(max = 100, message = "Project name must be at most 100 characters")
        String name,

        String description
) {
}
