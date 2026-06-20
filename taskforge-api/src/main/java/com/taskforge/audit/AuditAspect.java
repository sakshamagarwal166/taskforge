package com.taskforge.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskforge.auth.SecurityUtils;
import com.taskforge.common.exception.ResourceNotFoundException;
import com.taskforge.multitenancy.TenantContext;
import com.taskforge.tenant.Tenant;
import com.taskforge.tenant.TenantRepository;
import com.taskforge.user.User;
import com.taskforge.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    @SuppressWarnings("unchecked")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object rawOldValue = AuditContext.getOldValue();

        Object result = joinPoint.proceed();

        try {
            Map<String, Object> oldValue = rawOldValue != null
                    ? objectMapper.convertValue(rawOldValue, Map.class) : null;
            Map<String, Object> newValue = objectMapper.convertValue(result, Map.class);
            UUID entityId = UUID.fromString(String.valueOf(newValue.get("id")));

            saveAuditLog(auditable, entityId, oldValue, newValue);
        } catch (Exception e) {
            log.warn("Failed to create audit log for {}.{}: {}",
                    auditable.entity(), auditable.action(), e.getMessage());
        } finally {
            AuditContext.clear();
        }

        return result;
    }

    private void saveAuditLog(Auditable auditable, UUID entityId,
                               Map<String, Object> oldValue, Map<String, Object> newValue) {
        UUID tenantId = TenantContext.requireCurrentTenant();
        UUID userId = SecurityUtils.getCurrentUser().id();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + tenantId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        AuditLog auditLog = new AuditLog();
        auditLog.setTenant(tenant);
        auditLog.setUser(user);
        auditLog.setAction(auditable.action());
        auditLog.setEntityType(auditable.entity());
        auditLog.setEntityId(entityId);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);

        auditLogRepository.save(auditLog);
    }
}
