package com.smartcampus.notification.event;

public record RoomBecameAvailableEvent(
        Long roomId,
        String roomName,
        String availableFrom
) {}