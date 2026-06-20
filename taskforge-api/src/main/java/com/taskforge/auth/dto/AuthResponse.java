package com.taskforge.auth.dto;

import com.taskforge.user.Role;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        Role role,
        UUID tenantId,
        String tenantSlug
) {
}
