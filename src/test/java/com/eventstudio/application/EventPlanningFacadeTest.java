package com.eventstudio.application;

import com.eventstudio.domain.EventType;
import com.eventstudio.storage.InMemoryEventRepository;
import com.eventstudio.storage.InMemoryServicePackageRepository;
import java.time.LocalDate;

public final class EventPlanningFacadeTest {
    private static final LocalDate DATE = LocalDate.of(2026, 12, 1);

    private EventPlanningFacadeTest() {
    }

    public static void main(String[] args) {
        createEventWithLinkedStarterPackage();
        invalidEventCreatesNoPackage();
        updateKeepsStarterPackage();
        System.out.println("3 EventPlanningFacade scenarios passed");
    }

    private static void createEventWithLinkedStarterPackage() {
        InMemoryServicePackageRepository packageStorage = new InMemoryServicePackageRepository();
        EventPlanningFacade facade = new EventPlanningFacade(
            new EventCatalog(new InMemoryEventRepository()), new PackageCatalog(packageStorage));
        EventOverview overview = facade.createEvent("Workshop", DATE, 30, EventType.WORKSHOP);
        check(overview.starterPackage().eventId().equals(overview.event().id()),
            "starter package must belong to the created event");
        check(facade.listEvents().size() == 1, "facade must expose an event overview");
        check(packageStorage.findByEventId(overview.event().id()).size() == 1,
            "starter package must be saved exactly once");
    }

    private static void invalidEventCreatesNoPackage() {
        InMemoryEventRepository eventStorage = new InMemoryEventRepository();
        InMemoryServicePackageRepository packageStorage = new InMemoryServicePackageRepository();
        EventPlanningFacade facade = new EventPlanningFacade(
            new EventCatalog(eventStorage), new PackageCatalog(packageStorage));
        try {
            facade.createEvent("Workshop", DATE, 61, EventType.WORKSHOP);
            throw new AssertionError("expected workshop capacity error");
        } catch (IllegalArgumentException expected) {
            check(eventStorage.findAll().isEmpty(), "invalid event must not be saved");
            check(facade.listEvents().isEmpty(), "invalid flow must not create an overview");
        }
    }

    private static void updateKeepsStarterPackage() {
        EventPlanningFacade facade = new EventPlanningFacade(
            new EventCatalog(new InMemoryEventRepository()),
            new PackageCatalog(new InMemoryServicePackageRepository()));
        EventOverview created = facade.createEvent("Workshop", DATE, 30, EventType.WORKSHOP);
        EventOverview updated = facade.updateEvent(created.event().id(), "New name", DATE, 40, EventType.WORKSHOP);
        check(updated.event().title().equals("New name"), "updated title must appear in overview");
        check(updated.starterPackage().id().equals(created.starterPackage().id()),
            "editing the event must preserve its package");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
