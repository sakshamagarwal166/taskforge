package com.taskforge.project;

import com.taskforge.project.dto.CreateProjectRequest;
import com.taskforge.project.dto.ProjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tenants/{tenantId}/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management APIs")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project with default board columns")
    public ResponseEntity<ProjectResponse> createProject(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List projects in a tenant (paginated)")
    public ResponseEntity<Page<ProjectResponse>> getProjects(
            @PathVariable UUID tenantId,
            Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectsByTenant(tenantId, pageable));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable UUID tenantId,
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getProjectById(tenantId, projectId));
    }
}
