package com.quizplatform.backend.repository;

import com.quizplatform.backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findAllByOrderByCreatedAtDesc();

    @Query("SELECT q FROM Quiz q WHERE " +
            "(:q IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(q.description) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "AND (:categoryId IS NULL OR q.category.id = :categoryId) " +
            "AND (:difficulty IS NULL OR q.difficulty = :difficulty)")
    List<Quiz> search(@Param("q") String q, @Param("categoryId") Long categoryId,
                      @Param("difficulty") com.quizplatform.backend.enums.Difficulty difficulty);

    long countByStatus(com.quizplatform.backend.enums.QuizStatus status);
}