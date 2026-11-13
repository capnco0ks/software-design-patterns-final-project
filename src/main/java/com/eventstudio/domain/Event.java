package com.eventstudio.domain;

import java.time.LocalDate;
import java.util.UUID;

public sealed interface Event permits WorkshopEvent, ConcertEvent {
    UUID id();

    String title();

    LocalDate date();

    int capacity();

    EventType type();
}
