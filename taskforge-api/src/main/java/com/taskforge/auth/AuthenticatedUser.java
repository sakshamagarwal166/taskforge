package com.taskforge.auth;

import com.taskforge.user.Role;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        UUID tenantId,
        String email,
        Role role
) {
}
