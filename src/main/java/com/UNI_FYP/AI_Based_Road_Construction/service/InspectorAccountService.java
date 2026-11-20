package com.UNI_FYP.AI_Based_Road_Construction.service;

import com.UNI_FYP.AI_Based_Road_Construction.dto.InspectorCreateRequest;
import com.UNI_FYP.AI_Based_Road_Construction.dto.InspectorResponse;
import com.UNI_FYP.AI_Based_Road_Construction.entity.User;
import com.UNI_FYP.AI_Based_Road_Construction.entity.enums.UserRole;
import com.UNI_FYP.AI_Based_Road_Construction.exception.AppException;
import com.UNI_FYP.AI_Based_Road_Construction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class InspectorAccountService {

    private final UserRepository   userRepository;
    private final PasswordEncoder  passwordEncoder;
    private final AuditLogService  auditLogService;

    @Transactional
    public InspectorResponse create(InspectorCreateRequest request, User admin) {
        String email = request.getEmail().trim();

        if (userRepository.existsByEmail(email)) {
            throw new AppException("Email already registered", HttpStatus.CONFLICT);
        }

        User inspector = userRepository.save(User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.INSPECTOR)
                .enabled(true)
                .build());

        auditLogService.log(admin, "CREATE_INSPECTOR", "User", inspector.getId(), inspector.getEmail());

        return InspectorResponse.builder()
                .id(inspector.getId())
                .fullName(inspector.getFullName())
                .email(inspector.getEmail())
                .build();
    }
}