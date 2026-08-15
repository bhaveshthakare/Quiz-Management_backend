package com.quizplatform.backend.service;

import com.quizplatform.backend.dto.NotificationDtos.NotificationResponse;
import com.quizplatform.backend.entity.Notification;
import com.quizplatform.backend.entity.User;
import com.quizplatform.backend.enums.NotificationType;
import com.quizplatform.backend.exception.ApiException;
import com.quizplatform.backend.repository.NotificationRepository;
import com.quizplatform.backend.repository.UserRepository;
import com.quizplatform.backend.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification create(User user, NotificationType type, String message) {
        return notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .isRead(false)
                .build());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list() {
        User user = currentUser();
        return notificationRepository.findByUserOrderBySentAtDesc(user).stream()
                .map(n -> new NotificationResponse(n.getId(), n.getType(), n.getMessage(),
                        n.getIsRead(), n.getSentAt()))
                .toList();
    }

    @Transactional
    public void markRead(Long id) {
        User user = currentUser();
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!n.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this notification");
        }
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllRead() {
        User user = currentUser();
        List<Notification> all = notificationRepository.findByUserOrderBySentAtDesc(user);
        all.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(all);
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return notificationRepository.countByUserAndIsReadFalse(currentUser());
    }

    private User currentUser() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}