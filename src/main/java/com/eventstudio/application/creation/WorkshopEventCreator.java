package com.eventstudio.application.creation;

import com.eventstudio.domain.Event;
import com.eventstudio.domain.WorkshopEvent;
import java.time.LocalDate;
import java.util.UUID;

public final class WorkshopEventCreator extends EventCreator {
    @Override
    protected Event createEvent(UUID id, String title, LocalDate date, int capacity) {
        if (capacity > 60) {
            throw new IllegalArgumentException("Для мастер-класса доступно не больше 60 мест");
        }
        return new WorkshopEvent(id, title, date, capacity);
    }
}
