package com.eventstudio.domain;

import java.util.UUID;

public record Booking(UUID id, UUID eventId, UUID packageId, UUID participantId, BookingStatus status) {
}
