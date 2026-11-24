package com.smartcampus.notification.service;

import com.smartcampus.notification.domain.Notification;
import com.smartcampus.notification.domain.NotificationType;

import java.util.List;

public interface NotificationService {
    Notification createAndSend(Long userId, Long reservationId, NotificationType type, String message);
    List<Notification> findForUser(Long userId);
}