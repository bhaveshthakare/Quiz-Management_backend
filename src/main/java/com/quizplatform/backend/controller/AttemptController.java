package com.quizplatform.backend.controller;

import com.quizplatform.backend.dto.AttemptDtos.*;
import com.quizplatform.backend.service.AttemptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/quizzes/{quizId}/start")
    public ResponseEntity<StartAttemptResponse> start(@PathVariable Long quizId) {
        return ResponseEntity.ok(attemptService.start(quizId));
    }

    @PostMapping("/attempts/{id}/answer")
    public ResponseEntity<Map<String, String>> saveAnswer(@PathVariable Long id,
                                                          @Valid @RequestBody AnswerRequest req) {
        attemptService.saveAnswer(id, req);
        return ResponseEntity.ok(Map.of("message", "Answer saved"));
    }

    @PostMapping("/attempts/{id}/submit")
    public ResponseEntity<AttemptResult> submit(@PathVariable Long id) {
        return ResponseEntity.ok(attemptService.submit(id));
    }

    @GetMapping("/attempts")
    public ResponseEntity<List<AttemptSummary>> myHistory() {
        return ResponseEntity.ok(attemptService.myHistory());
    }

    @GetMapping("/attempts/{id}")
    public ResponseEntity<AttemptResult> myResult(@PathVariable Long id) {
        return ResponseEntity.ok(attemptService.myResult(id));
    }

    @GetMapping("/admin/attempts")
    public ResponseEntity<List<AttemptSummary>> adminList() {
        return ResponseEntity.ok(attemptService.adminList());
    }

    @GetMapping("/admin/attempts/{id}")
    public ResponseEntity<AttemptResult> adminResult(@PathVariable Long id) {
        return ResponseEntity.ok(attemptService.adminResult(id));
    }
}