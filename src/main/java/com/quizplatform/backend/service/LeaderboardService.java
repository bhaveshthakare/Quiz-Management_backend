package com.quizplatform.backend.service;

import com.quizplatform.backend.dto.LeaderboardDtos.LeaderboardEntry;
import com.quizplatform.backend.entity.Attempt;
import com.quizplatform.backend.enums.AttemptStatus;
import com.quizplatform.backend.repository.AttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final AttemptRepository attemptRepository;

    public LeaderboardService(AttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> overall() {
        return build(null, null);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> byCategory(Long categoryId) {
        return build(categoryId, null);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> byPeriod(String period) {
        return build(null, period);
    }

    private List<LeaderboardEntry> build(Long categoryId, String period) {
        LocalDateTime since = switch (period == null ? "" : period) {
            case "weekly" -> LocalDateTime.now().minusWeeks(1);
            case "monthly" -> LocalDateTime.now().minusMonths(1);
            default -> null;
        };

        List<Attempt> attempts = attemptRepository.findAll().stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS)
                .filter(a -> categoryId == null || a.getQuiz().getCategory().getId().equals(categoryId))
                .filter(a -> since == null || (a.getCompletedAt() != null && a.getCompletedAt().isAfter(since)))
                .toList();

        Map<Long, List<Attempt>> byUser = attempts.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        List<LeaderboardEntry> entries = byUser.entrySet().stream()
                .map(e -> {
                    List<Attempt> list = e.getValue();
                    double avg = list.stream().mapToDouble(a -> a.getPercentage().doubleValue()).average().orElse(0);
                    double highest = list.stream().mapToDouble(a -> a.getPercentage().doubleValue()).max().orElse(0);
                    return new LeaderboardEntry(e.getKey(), 0,
                            list.get(0).getUser().getUserName(),
                            round1(avg), round1(highest), (long) list.size());
                })
                .sorted(Comparator.comparingDouble(LeaderboardEntry::averageScore).reversed()
                        .thenComparing(Comparator.comparingDouble(LeaderboardEntry::highestScore).reversed()))
                .toList();

        for (int i = 0; i < entries.size(); i++) {
            entries = setRank(entries, i);
        }
        return entries;
    }

    private List<LeaderboardEntry> setRank(List<LeaderboardEntry> list, int index) {
        LeaderboardEntry e = list.get(index);
        LeaderboardEntry updated = new LeaderboardEntry(e.userId(), index + 1, e.name(),
                e.averageScore(), e.highestScore(), e.quizzesCompleted());
        List<LeaderboardEntry> result = new java.util.ArrayList<>(list);
        result.set(index, updated);
        return result;
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}