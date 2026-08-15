package com.quizplatform.backend.dto;

import java.util.List;

public record AnalyticsDtos() {

    public record StatsSummary(
            long totalStudents,
            long totalQuizzes,
            long publishedQuizzes,
            long draftQuizzes,
            long totalQuestions,
            long totalAttempts,
            long passedAttempts,
            long failedAttempts,
            long averageScore) {}

    public record ChartPoint(String label, long value) {}

    public record AnalyticsResponse(
            StatsSummary stats,
            List<ChartPoint> attemptsOverTime,
            List<ChartPoint> registrations,
            List<ChartPoint> passFailRatio,
            List<ChartPoint> averageScoresByQuiz,
            List<ChartPoint> popularQuizzes,
            List<ChartPoint> popularCategories) {}
}