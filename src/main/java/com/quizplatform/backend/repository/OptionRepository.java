package com.quizplatform.backend.repository;

import com.quizplatform.backend.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionRepository extends JpaRepository<Option, Long> {

    List<Option> findByQuestionIdOrderByIdAsc(Long questionId);
}