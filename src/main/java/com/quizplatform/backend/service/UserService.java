package com.quizplatform.backend.service;

import com.quizplatform.backend.dto.UserDtos.*;
import com.quizplatform.backend.entity.Attempt;
import com.quizplatform.backend.entity.User;
import com.quizplatform.backend.enums.AttemptStatus;
import com.quizplatform.backend.enums.Role;
import com.quizplatform.backend.enums.UserStatus;
import com.quizplatform.backend.exception.ApiException;
import com.quizplatform.backend.repository.AttemptRepository;
import com.quizplatform.backend.repository.UserRepository;
import com.quizplatform.backend.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AttemptRepository attemptRepository;

    public UserService(UserRepository userRepository, AttemptRepository attemptRepository) {
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentSummary> listStudents() {
        return userRepository.findByRoleOrderByCreatedAtDesc(Role.STUDENT).stream()
                .map(u -> new StudentSummary(u.getId(), u.getUserName(), u.getEmail(),
                        u.getCreatedAt(), u.getStatus()))
                .toList();
    }

    @Transactional
    public StudentSummary setStatus(Long id, StatusRequest req) {
        User user = findStudent(id);
        if (user.getId().equals(SecurityUtils.currentUserId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot change your own status");
        }
        user.setStatus(req.status());
        userRepository.save(user);
        return new StudentSummary(user.getId(), user.getUserName(), user.getEmail(),
                user.getCreatedAt(), user.getStatus());
    }

    @Transactional
    public void delete(Long id) {
        User user = findStudent(id);
        if (user.getId().equals(SecurityUtils.currentUserId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }
        if (!attemptRepository.findByUserOrderByStartedAtDesc(user).isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Cannot delete a student who has quiz attempts");
        }
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public ProfileResponse profile(Long id) {
        User user = findStudent(id);
        List<Attempt> attempts = attemptRepository.findByUserOrderByStartedAtDesc(user)
                .stream().filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS).toList();
        long distinctQuizzes = attempts.stream().map(a -> a.getQuiz().getId()).distinct().count();
        long passed = attempts.stream().filter(a -> a.getStatus() == AttemptStatus.PASSED).count();
        long failed = attempts.size() - passed;
        double avg = attempts.isEmpty() ? 0
                : attempts.stream().mapToDouble(a -> a.getPercentage().doubleValue()).average().orElse(0);
        double highest = attempts.isEmpty() ? 0
                : attempts.stream().mapToDouble(a -> a.getPercentage().doubleValue()).max().orElse(0);
        long answered = attempts.stream()
                .mapToLong(a -> (long) a.getCorrectAnswers() + a.getIncorrectAnswers()).sum();
        List<ProfileAttempt> attemptList = attempts.stream()
                .map(a -> new ProfileAttempt(a.getId(), a.getQuiz().getTitle(), a.getCompletedAt(),
                        a.getCorrectAnswers(), a.getPercentage(), a.getStatus()))
                .toList();
        return new ProfileResponse(
                new ProfileUser(user.getUserName(), user.getEmail(), user.getCreatedAt()),
                distinctQuizzes, passed, failed,
                round1(avg), round1(highest), answered, attemptList);
    }

    private User findStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        if (user.getRole() != Role.STUDENT) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Student not found");
        }
        return user;
    }

    private BigDecimal round1(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP);
    }
}