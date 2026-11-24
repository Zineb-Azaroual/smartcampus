package com.smartcampus.notification.service.impl;

import com.smartcampus.notification.domain.Notification;
import com.smartcampus.notification.domain.NotificationStatus;
import com.smartcampus.notification.domain.NotificationType;
import com.smartcampus.notification.repository.NotificationRepository;
import com.smartcampus.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationServiceImpl(NotificationRepository repository,
                                   SimpMessagingTemplate messagingTemplate) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public Notification createAndSend(Long userId, Long reservationId,
                                      NotificationType type, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setReservationId(reservationId);
        n.setType(type);
        n.setMessage(message);
        n.setStatus(NotificationStatus.PENDING);
        n = repository.save(n);

        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId),
                    "/queue/notifications",
                    n
            );
            n.setStatus(NotificationStatus.SENT);
            n.setSentAt(LocalDateTime.now());
            log.info("Notification envoyée à user {} : {}", userId, message);
        } catch (Exception e) {
            n.setStatus(NotificationStatus.FAILED);
            log.error("Échec envoi notification", e);
        }
        return repository.save(n);
    }

    @Override
    public List<Notification> findForUser(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}