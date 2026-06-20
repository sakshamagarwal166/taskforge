package com.taskforge.task;

import com.taskforge.task.dto.CreateTaskRequest;
import com.taskforge.task.dto.MoveTaskRequest;
import com.taskforge.task.dto.TaskResponse;
import com.taskforge.task.dto.UpdateTaskRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants/{tenantId}/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management APIs")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task in a project")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID tenantId,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(tenantId, projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all tasks in a project")
    public ResponseEntity<List<TaskResponse>> getTasks(
            @PathVariable UUID tenantId,
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(tenantId, projectId));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Update a task")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID tenantId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(tenantId, projectId, taskId, request));
    }

    @PatchMapping("/{taskId}/move")
    @Operation(summary = "Move a task to a different column or position")
    public ResponseEntity<TaskResponse> moveTask(
            @PathVariable UUID tenantId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody MoveTaskRequest request) {
        return ResponseEntity.ok(taskService.moveTask(tenantId, projectId, taskId, request));
    }
}
