package com.quizplatform.backend.controller;

import com.quizplatform.backend.dto.AnalyticsDtos.AnalyticsResponse;
import com.quizplatform.backend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<AnalyticsResponse> dashboard() {
        return ResponseEntity.ok(analyticsService.dashboard());
    }
}