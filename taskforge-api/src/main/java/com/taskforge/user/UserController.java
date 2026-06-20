package com.taskforge.user;

import com.taskforge.user.dto.CreateUserRequest;
import com.taskforge.user.dto.UserResponse;
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
@RequestMapping("/api/tenants/{tenantId}/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user in a tenant")
    public ResponseEntity<UserResponse> createUser(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List users in a tenant (paginated)")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @PathVariable UUID tenantId,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByTenant(tenantId, pageable));
    }
}
