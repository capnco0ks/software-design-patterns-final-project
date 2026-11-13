package com.eventstudio.application.creation;

import com.eventstudio.domain.ConcertEvent;
import com.eventstudio.domain.Event;
import java.time.LocalDate;
import java.util.UUID;

public final class ConcertEventCreator extends EventCreator {
    @Override
    protected Event createEvent(UUID id, String title, LocalDate date, int capacity) {
        if (capacity > 5000) {
            throw new IllegalArgumentException("Для концерта доступно не больше 5000 мест");
        }
        return new ConcertEvent(id, title, date, capacity);
    }
}
