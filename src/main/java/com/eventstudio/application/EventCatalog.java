package com.eventstudio.application;

import com.eventstudio.domain.Event;
import com.eventstudio.domain.EventType;
import com.eventstudio.application.creation.ConcertEventCreator;
import com.eventstudio.application.creation.EventCreator;
import com.eventstudio.application.creation.WorkshopEventCreator;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Event operations that select a concrete Factory Method creator by event type. */
public final class EventCatalog {
    private final EventRepository repository;
    private final Map<EventType, EventCreator> creators;

    public EventCatalog(EventRepository repository) {
        this(repository, Map.of(
            EventType.WORKSHOP, new WorkshopEventCreator(),
            EventType.CONCERT, new ConcertEventCreator()
        ));
    }

    public EventCatalog(EventRepository repository, Map<EventType, EventCreator> creators) {
        this.repository = repository;
        this.creators = Map.copyOf(creators);
    }

    public List<Event> list() {
        return repository.findAll();
    }

    public Optional<Event> find(UUID id) {
        return repository.findById(id);
    }

    public Event create(String title, LocalDate date, int capacity, EventType type) {
        Event event = creatorFor(type).create(UUID.randomUUID(), title, date, capacity);
        repository.save(event);
        return event;
    }

    public Event update(UUID id, String title, LocalDate date, int capacity, EventType type) {
        if (repository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Мероприятие не найдено");
        }
        Event event = creatorFor(type).create(id, title, date, capacity);
        repository.save(event);
        return event;
    }

    private EventCreator creatorFor(EventType type) {
        if (type == null) {
            throw new IllegalArgumentException("Выберите тип мероприятия");
        }
        EventCreator creator = creators.get(type);
        if (creator == null) {
            throw new IllegalArgumentException("Выберите тип мероприятия");
        }
        return creator;
    }
}
