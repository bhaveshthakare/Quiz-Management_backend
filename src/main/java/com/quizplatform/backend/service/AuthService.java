package com.quizplatform.backend.service;

import com.quizplatform.backend.dto.AuthDtos.*;
import com.quizplatform.backend.entity.PasswordResetToken;
import com.quizplatform.backend.entity.User;
import com.quizplatform.backend.enums.Role;
import com.quizplatform.backend.enums.UserStatus;
import com.quizplatform.backend.exception.ApiException;
import com.quizplatform.backend.repository.PasswordResetTokenRepository;
import com.quizplatform.backend.repository.UserRepository;
import com.quizplatform.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.frontend-origin}")
    private String frontendOrigin;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "An account with this email already exists");
        }
        User user = userRepository.save(User.builder()
                .userName(req.name().trim())
                .email(req.email().toLowerCase().trim())
                .password(passwordEncoder.encode(req.password()))
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .build());
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your account has been deactivated. Contact the administrator.");
        }
        return toResponse(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        userRepository.findByEmailIgnoreCase(req.email()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            tokenRepository.save(PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build());
            String link = frontendOrigin + "/auth/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUserName(), link);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        PasswordResetToken reset = tokenRepository.findByToken(req.token())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token"));
        if (reset.getUsed()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This reset token has already been used");
        }
        if (reset.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This reset token has expired");
        }
        User user = reset.getUser();
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        reset.setUsed(true);
        tokenRepository.save(reset);
    }

    private AuthResponse toResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getUserName(), user.getRole());
        return new AuthResponse(user.getId(), token, user.getUserName(), user.getEmail(), user.getRole().name());
    }
}