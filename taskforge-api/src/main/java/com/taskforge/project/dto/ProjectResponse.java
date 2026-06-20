package com.taskforge.project.dto;

import com.taskforge.project.Project;
import com.taskforge.project.ProjectStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID tenantId,
        String name,
        String description,
        String projectKey,
        UUID ownerId,
        ProjectStatus status,
        List<BoardColumnResponse> columns,
        LocalDateTime createdAt
) {
    public static ProjectResponse from(Project project) {
        List<BoardColumnResponse> cols = project.getColumns().stream()
                .map(BoardColumnResponse::from)
                .toList();

        return new ProjectResponse(
                project.getId(),
                project.getTenant().getId(),
                project.getName(),
                project.getDescription(),
                project.getProjectKey(),
                project.getOwner().getId(),
                project.getStatus(),
                cols,
                project.getCreatedAt()
        );
    }
}
