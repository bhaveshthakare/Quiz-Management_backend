package com.quizplatform.backend.dto;

import com.quizplatform.backend.enums.AttemptStatus;
import com.quizplatform.backend.enums.Difficulty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AttemptDtos() {

    public record AttemptOption(Long optionId, String optionText) {}

    public record AttemptQuestion(
            Long questionId,
            String questionText,
            Difficulty difficulty,
            Long selectedOptionId,
            List<AttemptOption> options) {}

    public record StartAttemptResponse(
            Long attemptId,
            String quizTitle,
            LocalDateTime deadline,
            List<AttemptQuestion> questions) {}

    public record AnswerRequest(
            @NotNull Long questionId,
            Long selectedOptionId) {}

    public record AttemptSummary(
            Long id,
            Long studentId,
            String studentName,
            String quizTitle,
            String category,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            Integer score,
            BigDecimal percentage,
            Integer correctAnswers,
            Integer incorrectAnswers,
            Integer unanswered,
            Integer timeTaken,
            AttemptStatus status) {}

    public record ReviewOption(Long optionId, String optionText, Boolean isCorrect, Boolean selected) {}

    public record ReviewItem(
            Long questionId,
            String questionText,
            Integer marks,
            Boolean isCorrect,
            Boolean answered,
            String explanation,
            List<ReviewOption> options) {}

    public record AttemptResult(
            Long attemptId,
            String quizTitle,
            Boolean passed,
            BigDecimal percentage,
            AttemptStatus status,
            Integer totalQuestions,
            Integer correctAnswers,
            Integer incorrectAnswers,
            Integer unanswered,
            Integer timeTaken,
            Long certificateId,
            List<ReviewItem> review) {}
}