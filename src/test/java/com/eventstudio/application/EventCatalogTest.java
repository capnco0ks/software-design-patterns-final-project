package com.eventstudio.application;

import com.eventstudio.domain.Event;
import com.eventstudio.domain.EventType;
import com.eventstudio.storage.InMemoryEventRepository;
import java.time.LocalDate;

public final class EventCatalogTest {
    private static final LocalDate DATE = LocalDate.of(2026, 12, 1);

    private EventCatalogTest() {
    }

    public static void main(String[] args) {
        createAndFind();
        updateKeepsIdentity();
        rejectInvalidCapacityWithoutSaving();
        rejectBlankTitle();
        System.out.println("4 business logic scenarios passed");
    }

    private static void createAndFind() {
        EventCatalog catalog = new EventCatalog(new InMemoryEventRepository());
        Event created = catalog.create("Workshop", DATE, 20, EventType.WORKSHOP);
        check(catalog.list().size() == 1, "created event must appear in list");
        check(catalog.find(created.id()).orElseThrow().title().equals("Workshop"), "created event must be findable");
    }

    private static void updateKeepsIdentity() {
        EventCatalog catalog = new EventCatalog(new InMemoryEventRepository());
        Event created = catalog.create("Workshop", DATE, 20, EventType.WORKSHOP);
        Event updated = catalog.update(created.id(), "Concert", DATE.plusDays(1), 80, EventType.CONCERT);
        check(updated.id().equals(created.id()), "editing must preserve event ID");
        check(catalog.list().size() == 1, "editing must not duplicate an event");
        check(catalog.find(created.id()).orElseThrow().capacity() == 80, "edited values must be stored");
    }

    private static void rejectInvalidCapacityWithoutSaving() {
        EventCatalog catalog = new EventCatalog(new InMemoryEventRepository());
        expectInvalid(() -> catalog.create("Workshop", DATE, 0, EventType.WORKSHOP));
        check(catalog.list().isEmpty(), "invalid event must not be stored");
    }

    private static void rejectBlankTitle() {
        EventCatalog catalog = new EventCatalog(new InMemoryEventRepository());
        expectInvalid(() -> catalog.create("  ", DATE, 20, EventType.WORKSHOP));
        check(catalog.list().isEmpty(), "blank title must not be stored");
    }

    private static void expectInvalid(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("expected IllegalArgumentException");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
