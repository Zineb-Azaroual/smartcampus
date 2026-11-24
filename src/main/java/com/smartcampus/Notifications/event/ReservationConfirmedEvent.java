package com.smartcampus.notification.event;

public record ReservationConfirmedEvent(
        Long reservationId,
        Long userId,
        String roomName,
        String startTime
) {}