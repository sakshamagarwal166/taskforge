package com.taskforge.user.dto;

import com.taskforge.user.Role;
import com.taskforge.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID tenantId,
        String email,
        String firstName,
        String lastName,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
