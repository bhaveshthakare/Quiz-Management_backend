package com.quizplatform.backend.dto;

public record LeaderboardDtos() {

    public record LeaderboardEntry(
            Long userId,
            Integer rank,
            String name,
            Double averageScore,
            Double highestScore,
            Long quizzesCompleted) {}
}