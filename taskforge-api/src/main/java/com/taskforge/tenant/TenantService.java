package com.taskforge.tenant;

import com.taskforge.common.exception.DuplicateResourceException;
import com.taskforge.common.exception.ResourceNotFoundException;
import com.taskforge.tenant.dto.CreateTenantRequest;
import com.taskforge.tenant.dto.TenantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        if (tenantRepository.existsBySlug(request.slug())) {
            throw new DuplicateResourceException("Tenant with slug '" + request.slug() + "' already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.name());
        tenant.setSlug(request.slug());

        Tenant saved = tenantRepository.save(tenant);
        return TenantResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantBySlug(String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with slug: " + slug));
        return TenantResponse.from(tenant);
    }
}
