package com.taskforge.auth;

import com.taskforge.auth.dto.AuthResponse;
import com.taskforge.auth.dto.LoginRequest;
import com.taskforge.auth.dto.RegisterRequest;
import com.taskforge.auth.jwt.JwtTokenProvider;
import com.taskforge.common.exception.BadRequestException;
import com.taskforge.common.exception.DuplicateResourceException;
import com.taskforge.common.exception.ResourceNotFoundException;
import com.taskforge.tenant.Tenant;
import com.taskforge.tenant.TenantRepository;
import com.taskforge.user.Role;
import com.taskforge.user.User;
import com.taskforge.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (tenantRepository.existsBySlug(request.tenantSlug())) {
            throw new DuplicateResourceException("Tenant slug '" + request.tenantSlug() + "' is already taken");
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.tenantName());
        tenant.setSlug(request.tenantSlug());
        tenant = tenantRepository.save(tenant);

        User owner = new User();
        owner.setTenant(tenant);
        owner.setEmail(request.email());
        owner.setPasswordHash(passwordEncoder.encode(request.password()));
        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setRole(Role.OWNER);
        owner = userRepository.save(owner);

        return buildAuthResponse(owner, tenant);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Tenant tenant = tenantRepository.findBySlug(request.tenantSlug())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + request.tenantSlug()));

        User user = userRepository.findByTenantIdAndEmail(tenant.getId(), request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        return buildAuthResponse(user, tenant);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        var claims = tokenProvider.getClaims(refreshToken);
        User user = userRepository.findById(tokenProvider.getUserId(claims))
                .orElseThrow(() -> new BadRequestException("User not found"));

        return buildAuthResponse(user, user.getTenant());
    }

    private AuthResponse buildAuthResponse(User user, Tenant tenant) {
        return new AuthResponse(
                tokenProvider.generateAccessToken(user),
                tokenProvider.generateRefreshToken(user),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                tenant.getId(),
                tenant.getSlug()
        );
    }
}
