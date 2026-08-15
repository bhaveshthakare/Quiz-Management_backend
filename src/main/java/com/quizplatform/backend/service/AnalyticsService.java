package com.quizplatform.backend.service;

import com.quizplatform.backend.dto.AnalyticsDtos.*;
import com.quizplatform.backend.entity.Attempt;
import com.quizplatform.backend.entity.User;
import com.quizplatform.backend.enums.AttemptStatus;
import com.quizplatform.backend.enums.QuizStatus;
import com.quizplatform.backend.enums.Role;
import com.quizplatform.backend.repository.AttemptRepository;
import com.quizplatform.backend.repository.QuestionRepository;
import com.quizplatform.backend.repository.QuizRepository;
import com.quizplatform.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    public  UserRepository userRepository;
    public QuizRepository quizRepository;
    public  QuestionRepository questionRepository;
    public AttemptRepository attemptRepository;

    public AnalyticsService(UserRepository userRepository,
                            QuizRepository quizRepository,
                            QuestionRepository questionRepository,
                            AttemptRepository attemptRepository) {
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse dashboard() {
        List<Attempt> attempts = attemptRepository.findAll().stream()
                .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS)
                .toList();
        List<User> students = userRepository.findByRoleOrderByCreatedAtDesc(Role.STUDENT);

        long passed = attempts.stream().filter(a -> a.getStatus() == AttemptStatus.PASSED).count();
        long failed = attempts.size() - passed;
        double avg = attempts.isEmpty() ? 0
                : attempts.stream().mapToDouble(a -> a.getPercentage().doubleValue()).average().orElse(0);

        StatsSummary stats = new StatsSummary(
                students.size(),
                quizRepository.count(),
                quizRepository.countByStatus(QuizStatus.PUBLISHED),
                quizRepository.countByStatus(QuizStatus.DRAFT),
                questionRepository.count(),
                attempts.size(),
                passed,
                failed,
                Math.round(avg));

        List<ChartPoint> attemptsOverTime = dailyCounts(14, attempts.stream()
                .filter(a -> a.getCompletedAt() != null)
                .collect(Collectors.toMap(a -> a.getCompletedAt().toLocalDate(), a -> 1, Integer::sum)));
        List<ChartPoint> registrations = dailyCounts(14, students.stream()
                .filter(u -> u.getCreatedAt() != null)
                .collect(Collectors.toMap(u -> u.getCreatedAt().toLocalDate(), u -> 1, Integer::sum)));

        List<ChartPoint> passFail = List.of(
                new ChartPoint("Passed", passed),
                new ChartPoint("Failed", failed));

        Map<Long, List<Attempt>> byQuiz = attempts.stream()
                .collect(Collectors.groupingBy(a -> a.getQuiz().getId()));
        List<ChartPoint> averageScores = byQuiz.entrySet().stream()
                .map(e -> {
                    String title = e.getValue().get(0).getQuiz().getTitle();
                    double avgScore = e.getValue().stream()
                            .mapToDouble(a -> a.getPercentage().doubleValue()).average().orElse(0);
                    return new ChartPoint(title, Math.round(avgScore));
                })
                .sorted(Comparator.comparingLong(ChartPoint::value).reversed())
                .limit(6)
                .toList();

        List<ChartPoint> popularQuizzes = byQuiz.entrySet().stream()
                .map(e -> new ChartPoint(e.getValue().get(0).getQuiz().getTitle(), e.getValue().size()))
                .sorted(Comparator.comparingLong(ChartPoint::value).reversed())
                .limit(5)
                .toList();

        Map<Long, Long> byCategory = attempts.stream()
                .collect(Collectors.groupingBy(a -> a.getQuiz().getCategory().getId(), Collectors.counting()));
        List<ChartPoint> popularCategories = byCategory.entrySet().stream()
                .map(e -> {
                    String name = attempts.stream()
                            .filter(a -> a.getQuiz().getCategory().getId().equals(e.getKey()))
                            .findFirst().map(a -> a.getQuiz().getCategory().getName()).orElse("Other");
                    return new ChartPoint(name, e.getValue());
                })
                .sorted(Comparator.comparingLong(ChartPoint::value).reversed())
                .limit(5)
                .toList();

        return new AnalyticsResponse(stats, attemptsOverTime, registrations, passFail,
                averageScores, popularQuizzes, popularCategories);
    }

    private List<ChartPoint> dailyCounts(int days, Map<LocalDate, Integer> counts) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");
        LocalDate today = LocalDate.now();
        List<ChartPoint> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            result.add(new ChartPoint(day.format(fmt), counts.getOrDefault(day, 0)));
        }
        return result;
    }
}