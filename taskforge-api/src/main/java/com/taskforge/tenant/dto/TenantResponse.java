package com.taskforge.tenant.dto;

import com.taskforge.tenant.Plan;
import com.taskforge.tenant.Tenant;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        Plan plan,
        boolean active,
        LocalDateTime createdAt
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getPlan(),
                tenant.isActive(),
                tenant.getCreatedAt()
        );
    }
}
