package com.quizplatform.backend.dto;

import com.quizplatform.backend.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationDtos() {

    public record NotificationResponse(
            Long id,
            NotificationType type,
            String message,
            Boolean isRead,
            LocalDateTime sentAt) {}
}