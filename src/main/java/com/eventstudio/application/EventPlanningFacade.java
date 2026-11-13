package com.eventstudio.application;

import com.eventstudio.domain.Event;
import com.eventstudio.domain.EventType;
import com.eventstudio.domain.ServicePackage;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Facade for the organizer's event planning flow across event and package subsystems. */
public final class EventPlanningFacade {
    private final EventCatalog events;
    private final PackageCatalog packages;

    public EventPlanningFacade(EventCatalog events, PackageCatalog packages) {
        this.events = events;
        this.packages = packages;
    }

    public EventOverview createEvent(String title, LocalDate date, int capacity, EventType type) {
        Event event = events.create(title, date, capacity, type);
        ServicePackage starter = packages.createStarter(event);
        return new EventOverview(event, starter);
    }

    public EventOverview updateEvent(UUID id, String title, LocalDate date, int capacity, EventType type) {
        Event event = events.update(id, title, date, capacity, type);
        return overviewFor(event);
    }

    public Optional<Event> findEvent(UUID id) {
        return events.find(id);
    }

    public List<EventOverview> listEvents() {
        return events.list().stream().map(this::overviewFor).toList();
    }

    private EventOverview overviewFor(Event event) {
        ServicePackage starter = packages.listForEvent(event.id()).stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("Пакет услуг не найден для мероприятия " + event.id()));
        return new EventOverview(event, starter);
    }
}
