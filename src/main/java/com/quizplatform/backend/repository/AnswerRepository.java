package com.quizplatform.backend.repository;

import com.quizplatform.backend.entity.Answer;
import com.quizplatform.backend.entity.Attempt;
import com.quizplatform.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Optional<Answer> findByAttemptAndQuestion(Attempt attempt, Question question);

    long countByAttempt(Attempt attempt);
}