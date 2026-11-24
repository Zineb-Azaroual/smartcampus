package com.smartcampus.notification.scheduler;

import com.smartcampus.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final NotificationService notificationService;

    public ReminderScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void sendReminders() {
        log.info("[Scheduler] Vérification des rappels de réservation...");
        // À remplacer par une vraie requête sur ReservationRepository
    }
}