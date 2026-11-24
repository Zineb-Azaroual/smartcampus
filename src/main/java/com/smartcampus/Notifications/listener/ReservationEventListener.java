package com.smartcampus.notification.listener;

import com.smartcampus.notification.domain.NotificationType;
import com.smartcampus.notification.event.ReservationCancelledEvent;
import com.smartcampus.notification.event.ReservationConfirmedEvent;
import com.smartcampus.notification.event.RoomBecameAvailableEvent;
import com.smartcampus.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventListener {

    private final NotificationService notificationService;

    public ReservationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onReservationConfirmed(ReservationConfirmedEvent event) {
        String msg = String.format(
                "Votre réservation #%d pour la salle %s débutant à %s est confirmée.",
                event.reservationId(), event.roomName(), event.startTime());
        notificationService.createAndSend(
                event.userId(), event.reservationId(),
                NotificationType.CONFIRMATION_RESERVATION, msg);
    }

    @EventListener
    public void onReservationCancelled(ReservationCancelledEvent event) {
        String msg = String.format(
                "Votre réservation #%d pour la salle %s a été annulée. Raison : %s",
                event.reservationId(), event.roomName(), event.reason());
        notificationService.createAndSend(
                event.userId(), event.reservationId(),
                NotificationType.ANNULATION_RESERVATION, msg);
    }

    @EventListener
    public void onRoomBecameAvailable(RoomBecameAvailableEvent event) {
        String msg = String.format(
                "La salle %s est maintenant disponible à partir de %s.",
                event.roomName(), event.availableFrom());
        notificationService.createAndSend(
                1L, null,
                NotificationType.SALLE_DISPONIBLE, msg);
    }
}