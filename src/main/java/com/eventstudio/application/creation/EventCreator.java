package com.eventstudio.application.creation;

import com.eventstudio.domain.Event;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Creator: shared creation workflow; concrete creators choose the Event product. */
public abstract class EventCreator {
    public final Event create(UUID id, String title, LocalDate date, int capacity) {
        Objects.requireNonNull(id, "event id");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Введите название мероприятия");
        }
        String cleanTitle = title.trim();
        if (cleanTitle.length() > 120) {
            throw new IllegalArgumentException("Название должно быть не длиннее 120 символов");
        }
        if (date == null) {
            throw new IllegalArgumentException("Выберите дату мероприятия");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("Количество мест должно быть положительным");
        }
        return createEvent(id, cleanTitle, date, capacity);
    }

    protected abstract Event createEvent(UUID id, String title, LocalDate date, int capacity);
}
