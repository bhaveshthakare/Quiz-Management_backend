package com.quizplatform.backend.repository;

import com.quizplatform.backend.entity.Notification;
import com.quizplatform.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderBySentAtDesc(User user);

    long countByUserAndIsReadFalse(User user);
}