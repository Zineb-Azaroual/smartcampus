package com.smartcampus.notification.web;

import com.smartcampus.notification.domain.Notification;
import com.smartcampus.notification.service.NotificationService;
import com.smartcampus.notification.web.dto.NotificationDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    public List<NotificationDto> getForUser(@PathVariable Long userId) {
        return notificationService.findForUser(userId).stream()
                .map(this::toDto)
                .toList();
    }

    private NotificationDto toDto(Notification n) {
        return new NotificationDto(
                n.getId(), n.getUserId(), n.getReservationId(),
                n.getType(), n.getStatus(), n.getMessage(),
                n.getCreatedAt(), n.getSentAt()
        );
    }
}