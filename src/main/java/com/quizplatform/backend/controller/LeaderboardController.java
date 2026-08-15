package com.quizplatform.backend.controller;

import com.quizplatform.backend.dto.LeaderboardDtos.LeaderboardEntry;
import com.quizplatform.backend.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<List<LeaderboardEntry>> overall() {
        return ResponseEntity.ok(leaderboardService.overall());
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<List<LeaderboardEntry>> byCategory(@PathVariable Long id) {
        return ResponseEntity.ok(leaderboardService.byCategory(id));
    }

    @GetMapping("/period")
    public ResponseEntity<List<LeaderboardEntry>> byPeriod(@RequestParam String period) {
        return ResponseEntity.ok(leaderboardService.byPeriod(period));
    }
}