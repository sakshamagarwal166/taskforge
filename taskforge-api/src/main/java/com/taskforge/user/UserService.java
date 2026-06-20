package com.taskforge.user;

import com.taskforge.common.exception.DuplicateResourceException;
import com.taskforge.common.exception.ResourceNotFoundException;
import com.taskforge.multitenancy.TenantContext;
import com.taskforge.tenant.Tenant;
import com.taskforge.tenant.TenantRepository;
import com.taskforge.user.dto.CreateUserRequest;
import com.taskforge.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        UUID tenantId = TenantContext.requireCurrentTenant();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + tenantId));

        if (userRepository.existsByTenantIdAndEmail(tenantId, request.email())) {
            throw new DuplicateResourceException(
                    "User with email '" + request.email() + "' already exists in this tenant");
        }

        User user = new User();
        user.setTenant(tenant);
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(request.role() != null ? request.role() : Role.MEMBER);

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }
}
