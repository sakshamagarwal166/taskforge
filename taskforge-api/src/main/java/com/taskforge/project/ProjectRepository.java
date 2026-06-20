package com.taskforge.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Page<Project> findAllByTenantId(UUID tenantId, Pageable pageable);

    boolean existsByTenantIdAndProjectKey(UUID tenantId, String projectKey);
}
