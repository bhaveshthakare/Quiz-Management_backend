package com.quizplatform.backend.dto;

import com.quizplatform.backend.enums.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record UserDtos() {

    public record StudentSummary(
            Long id,
            String name,
            String email,
            LocalDateTime createdAt,
            UserStatus status) {}

    public record StatusRequest(UserStatus status) {}

    public record ProfileAttempt(
            Long id,
            String quizTitle,
            LocalDateTime completedAt,
            Integer correctAnswers,
            BigDecimal percentage,
            com.quizplatform.backend.enums.AttemptStatus status) {}

    public record ProfileUser(String name, String email, LocalDateTime createdAt) {}

    public record ProfileResponse(
            ProfileUser user,
            long quizzesAttempted,
            long quizzesPassed,
            long quizzesFailed,
            BigDecimal averageScore,
            BigDecimal highestScore,
            long totalQuestionsAnswered,
            List<ProfileAttempt> attempts) {}
}