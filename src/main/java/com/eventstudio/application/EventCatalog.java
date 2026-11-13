package com.eventstudio.application;

import com.eventstudio.domain.Event;
import com.eventstudio.domain.EventType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Basic event operations. Asanali will replace direct construction with his Factory Method. */
public final class EventCatalog {
    private final EventRepository repository;

    public EventCatalog(EventRepository repository) {
        this.repository = repository;
    }

    public List<Event> list() {
        return repository.findAll();
    }

    public Optional<Event> find(UUID id) {
        return repository.findById(id);
    }

    public Event create(String title, LocalDate date, int capacity, EventType type) {
        validate(title, date, capacity, type);
        Event event = new Event(UUID.randomUUID(), title.trim(), date, capacity, type);
        repository.save(event);
        return event;
    }

    public Event update(UUID id, String title, LocalDate date, int capacity, EventType type) {
        if (repository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Мероприятие не найдено");
        }
        validate(title, date, capacity, type);
        Event event = new Event(id, title.trim(), date, capacity, type);
        repository.save(event);
        return event;
    }

    private static void validate(String title, LocalDate date, int capacity, EventType type) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Введите название мероприятия");
        }
        if (title.trim().length() > 120) {
            throw new IllegalArgumentException("Название должно быть не длиннее 120 символов");
        }
        if (date == null) {
            throw new IllegalArgumentException("Выберите дату мероприятия");
        }
        if (capacity < 1 || capacity > 10000) {
            throw new IllegalArgumentException("Количество мест должно быть от 1 до 10000");
        }
        if (type == null) {
            throw new IllegalArgumentException("Выберите тип мероприятия");
        }
    }
}
