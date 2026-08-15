package com.quizplatform.backend.controller;

import com.quizplatform.backend.dto.QuizDtos.*;
import com.quizplatform.backend.enums.Difficulty;
import com.quizplatform.backend.enums.QuizStatus;
import com.quizplatform.backend.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<List<QuizSummary>> list(@RequestParam(required = false) String q,
                                                  @RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) Difficulty difficulty,
                                                  @RequestParam(required = false) String duration,
                                                  @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(quizService.list(q, categoryId, difficulty, duration, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizDetail> detail(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.detail(id));
    }

    @PostMapping
    public ResponseEntity<QuizDetail> create(@Valid @RequestBody QuizRequest req) {
        return ResponseEntity.ok(quizService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizDetail> update(@PathVariable Long id, @Valid @RequestBody QuizRequest req) {
        return ResponseEntity.ok(quizService.update(id, req));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<QuizDetail> setStatus(@PathVariable Long id,
                                                @RequestBody Map<String, String> body) {
        QuizStatus status = QuizStatus.valueOf(body.getOrDefault("status", "PUBLISHED"));
        return ResponseEntity.ok(quizService.setStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        quizService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Quiz deleted"));
    }
}