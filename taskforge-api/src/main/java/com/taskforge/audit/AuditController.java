package com.taskforge.audit;

import com.taskforge.audit.dto.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit log APIs")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "List audit logs for the current tenant")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(Pageable pageable) {
        Page<AuditLogResponse> logs = auditLogRepository.findAll(pageable)
                .map(AuditLogResponse::from);
        return ResponseEntity.ok(logs);
    }
}
