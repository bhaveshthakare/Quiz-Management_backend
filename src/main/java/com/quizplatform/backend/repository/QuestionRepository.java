package com.quizplatform.backend.repository;

import com.quizplatform.backend.entity.Question;
import com.quizplatform.backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizOrderByIdAsc(Quiz quiz);

    long countByQuiz(Quiz quiz);
}