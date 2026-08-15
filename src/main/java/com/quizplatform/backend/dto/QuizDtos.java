package com.quizplatform.backend.dto;

import com.quizplatform.backend.enums.Difficulty;
import com.quizplatform.backend.enums.QuizStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuizDtos() {

    public record QuizRequest(
            @NotBlank @Size(max = 150) String title,
            String description,
            @NotNull Long categoryId,
            @NotNull Difficulty difficulty,
            @NotNull @Min(1) @Max(600) Integer duration,
            @NotNull @Min(0) @Max(100) Integer passingScore,
            @NotNull @Min(1) @Max(100) Integer maxAttempts,
            @NotNull QuizStatus status,
            @Size(max = 255) String thumbnail,
            Boolean negativeMarking,
            BigDecimal negativeMarkValue,
            LocalDateTime startDate,
            LocalDateTime endDate) {}

    public record QuizSummary(
            Long id,
            String title,
            String description,
            String thumbnail,
            CategoryRef category,
            Difficulty difficulty,
            Integer duration,
            Integer passingScore,
            Integer maxAttempts,
            QuizStatus status,
            long questionCount,
            Boolean negativeMarking,
            BigDecimal negativeMarkValue,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime createdAt) {

        public record CategoryRef(Long id, String name) {}
    }

    public record QuizDetail(
            Long id,
            String title,
            String description,
            String thumbnail,
            CategoryRef category,
            Difficulty difficulty,
            Integer duration,
            Integer passingScore,
            Integer maxAttempts,
            QuizStatus status,
            long questionCount,
            Boolean negativeMarking,
            BigDecimal negativeMarkValue,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime createdAt) {

        public record CategoryRef(Long id, String name) {}
    }
}