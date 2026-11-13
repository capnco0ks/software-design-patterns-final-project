package com.eventstudio.domain;

import java.time.LocalDate;
import java.util.UUID;

public record ConcertEvent(UUID id, String title, LocalDate date, int capacity) implements Event {
    @Override
    public EventType type() {
        return EventType.CONCERT;
    }
}
