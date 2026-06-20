package com.taskforge.project;

import com.taskforge.common.exception.ResourceNotFoundException;
import com.taskforge.project.dto.ProjectResponse;
import com.taskforge.project.dto.CreateProjectRequest;
import com.taskforge.tenant.Tenant;
import com.taskforge.tenant.TenantRepository;
import com.taskforge.user.User;
import com.taskforge.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(UUID tenantId, CreateProjectRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + tenantId));

        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.ownerId()));

        String projectKey = generateUniqueKey(tenantId, request.name());

        Project project = new Project();
        project.setTenant(tenant);
        project.setName(request.name());
        project.setDescription(request.description());
        project.setProjectKey(projectKey);
        project.setOwner(owner);

        addDefaultColumns(project);

        Project saved = projectRepository.save(project);
        return ProjectResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjectsByTenant(UUID tenantId, Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant not found: " + tenantId);
        }
        return projectRepository.findAllByTenantId(tenantId, pageable).map(ProjectResponse::from);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID tenantId, UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        if (!project.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }

        return ProjectResponse.from(project);
    }

    private String generateUniqueKey(UUID tenantId, String name) {
        String base = buildKeyFromName(name);

        String candidate = base;
        int suffix = 1;
        while (projectRepository.existsByTenantIdAndProjectKey(tenantId, candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private String buildKeyFromName(String name) {
        String[] words = name.trim().split("\\s+");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(3, words[0].length())).toUpperCase();
        }
        StringBuilder key = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty() && key.length() < 5) {
                key.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return key.toString();
    }

    private void addDefaultColumns(Project project) {
        String[][] defaults = {
                {"To Do", "#E2E8F0"},
                {"In Progress", "#FED7AA"},
                {"Done", "#BBF7D0"}
        };

        for (int i = 0; i < defaults.length; i++) {
            BoardColumn column = new BoardColumn();
            column.setProject(project);
            column.setName(defaults[i][0]);
            column.setPosition(i);
            column.setColor(defaults[i][1]);
            project.getColumns().add(column);
        }
    }
}
