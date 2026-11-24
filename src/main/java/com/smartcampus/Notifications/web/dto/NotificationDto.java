package com.smartcampus.notification.web.dto;

import com.smartcampus.notification.domain.NotificationStatus;
import com.smartcampus.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        Long userId,
        Long reservationId,
        NotificationType type,
        NotificationStatus status,
        String message,
        LocalDateTime createdAt,
        LocalDateTime sentAt
) {}