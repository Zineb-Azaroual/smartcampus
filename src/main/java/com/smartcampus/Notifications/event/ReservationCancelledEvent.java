package com.smartcampus.notification.event;

public record ReservationCancelledEvent(
        Long reservationId,
        Long userId,
        String roomName,
        String reason
) {}