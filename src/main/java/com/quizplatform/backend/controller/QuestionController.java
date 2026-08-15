package com.quizplatform.backend.controller;

import com.quizplatform.backend.dto.QuestionDtos.*;
import com.quizplatform.backend.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<List<QuestionResponse>> list(@PathVariable Long quizId) {
        return ResponseEntity.ok(questionService.listByQuiz(quizId));
    }

    @PostMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<QuestionResponse> create(@PathVariable Long quizId,
                                                   @Valid @RequestBody QuestionRequest req) {
        return ResponseEntity.ok(questionService.create(quizId, req));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<QuestionResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody QuestionRequest req) {
        return ResponseEntity.ok(questionService.update(id, req));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Question deleted"));
    }

    @PostMapping(value = "/quizzes/{quizId}/questions/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResponse> importQuestions(@PathVariable Long quizId,
                                                          @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(questionService.importQuestions(quizId, file));
    }
}