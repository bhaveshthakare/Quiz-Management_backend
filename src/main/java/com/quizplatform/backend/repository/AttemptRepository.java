package com.quizplatform.backend.repository;

import com.quizplatform.backend.entity.Attempt;
import com.quizplatform.backend.entity.Quiz;
import com.quizplatform.backend.entity.User;
import com.quizplatform.backend.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    Optional<Attempt> findFirstByUserAndQuizAndStatusOrderByStartedAtDesc(User user, Quiz quiz, AttemptStatus status);

    long countByUserAndQuiz(User user, Quiz quiz);

    long countByQuiz(Quiz quiz);

    long countByStatus(AttemptStatus status);

    List<Attempt> findByUserOrderByStartedAtDesc(User user);

    List<Attempt> findAllByOrderByStartedAtDesc();
}