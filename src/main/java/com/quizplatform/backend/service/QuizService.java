package com.quizplatform.backend.service;

import com.quizplatform.backend.dto.QuizDtos.*;
import com.quizplatform.backend.entity.Category;
import com.quizplatform.backend.entity.Quiz;
import com.quizplatform.backend.enums.Difficulty;
import com.quizplatform.backend.enums.QuizStatus;
import com.quizplatform.backend.exception.ApiException;
import com.quizplatform.backend.repository.AttemptRepository;
import com.quizplatform.backend.repository.CategoryRepository;
import com.quizplatform.backend.repository.QuestionRepository;
import com.quizplatform.backend.repository.QuizRepository;
import com.quizplatform.backend.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;

    public QuizService(QuizRepository quizRepository,
                       CategoryRepository categoryRepository,
                       QuestionRepository questionRepository,
                       AttemptRepository attemptRepository) {
        this.quizRepository = quizRepository;
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public List<QuizSummary> list(String q, Long categoryId, Difficulty difficulty,
                                  String duration, String sort) {
        boolean admin = SecurityUtils.isAdmin();
        List<Quiz> quizzes = quizRepository.search(q, categoryId, difficulty);
        if (!admin) {
            quizzes = quizzes.stream().filter(x -> x.getStatus() == QuizStatus.PUBLISHED).toList();
        }
        if (duration != null) {
            quizzes = quizzes.stream().filter(x -> matchesDuration(x.getDuration(), duration)).toList();
        }
        Comparator<Quiz> comp = switch (sort == null ? "recent" : sort) {
            case "popular" -> Comparator.comparingLong(this::attemptCount).reversed();
            default -> Comparator.comparing(Quiz::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        };
        return quizzes.stream().sorted(comp).map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public QuizDetail detail(Long id) {
        Quiz quiz = find(id);
        if (!SecurityUtils.isAdmin() && quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Quiz not found");
        }
        return toDetail(quiz);
    }

    @Transactional
    public QuizDetail create(QuizRequest req) {
        Quiz quiz = toEntity(new Quiz(), req);
        Quiz saved = quizRepository.save(quiz);
        return toDetail(saved);
    }

    @Transactional
    public QuizDetail update(Long id, QuizRequest req) {
        Quiz quiz = find(id);
        toEntity(quiz, req);
        Quiz saved = quizRepository.save(quiz);
        return toDetail(saved);
    }

    @Transactional
    public QuizDetail setStatus(Long id, QuizStatus status) {
        Quiz quiz = find(id);
        quiz.setStatus(status);
        Quiz saved = quizRepository.save(quiz);
        return toDetail(saved);
    }

    @Transactional
    public void delete(Long id) {
        Quiz quiz = find(id);
        if (attemptRepository.countByQuiz(quiz) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot delete a quiz that has attempts");
        }
        quizRepository.delete(quiz);
    }

    private Quiz toEntity(Quiz quiz, QuizRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Category not found"));
        quiz.setTitle(req.title().trim());
        quiz.setDescription(req.description());
        quiz.setCategory(category);
        quiz.setDifficulty(req.difficulty());
        quiz.setDuration(req.duration());
        quiz.setPassingScore(req.passingScore());
        quiz.setMaxAttempts(req.maxAttempts());
        quiz.setStatus(req.status());
        quiz.setThumbnail(req.thumbnail());
        quiz.setNegativeMarking(req.negativeMarking() != null ? req.negativeMarking() : false);
        quiz.setNegativeMarkValue(req.negativeMarkValue() != null ? req.negativeMarkValue() : java.math.BigDecimal.ZERO);
        quiz.setStartDate(req.startDate());
        quiz.setEndDate(req.endDate());
        return quiz;
    }

    private QuizSummary toSummary(Quiz q) {
        return new QuizSummary(q.getId(), q.getTitle(), q.getDescription(), q.getThumbnail(),
                new QuizSummary.CategoryRef(q.getCategory().getId(), q.getCategory().getName()),
                q.getDifficulty(), q.getDuration(), q.getPassingScore(), q.getMaxAttempts(),
                q.getStatus(), questionCount(q), q.getNegativeMarking(), q.getNegativeMarkValue(),
                q.getStartDate(), q.getEndDate(), q.getCreatedAt());
    }

    private QuizDetail toDetail(Quiz q) {
        return new QuizDetail(q.getId(), q.getTitle(), q.getDescription(), q.getThumbnail(),
                new QuizDetail.CategoryRef(q.getCategory().getId(), q.getCategory().getName()),
                q.getDifficulty(), q.getDuration(), q.getPassingScore(), q.getMaxAttempts(),
                q.getStatus(), questionCount(q), q.getNegativeMarking(), q.getNegativeMarkValue(),
                q.getStartDate(), q.getEndDate(), q.getCreatedAt());
    }

    private long questionCount(Quiz q) {
        return questionRepository.countByQuiz(q);
    }

    private long attemptCount(Quiz q) {
        return attemptRepository.countByQuiz(q);
    }

    private boolean matchesDuration(Integer minutes, String filter) {
        return switch (filter) {
            case "short" -> minutes <= 15;
            case "medium" -> minutes >= 16 && minutes <= 30;
            case "long" -> minutes > 30;
            default -> true;
        };
    }

    private Quiz find(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Quiz not found"));
    }
}