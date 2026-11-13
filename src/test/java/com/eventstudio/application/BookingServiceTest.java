package com.eventstudio.application;

import com.eventstudio.domain.Booking;
import com.eventstudio.domain.BookingStatus;
import com.eventstudio.domain.EventType;
import com.eventstudio.domain.PricingMode;
import com.eventstudio.storage.InMemoryBookingRepository;
import com.eventstudio.storage.InMemoryEventRepository;
import com.eventstudio.storage.InMemoryParticipantRepository;
import com.eventstudio.storage.InMemoryServicePackageRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class BookingServiceTest {
    private BookingServiceTest() {
    }

    public static void main(String[] args) {
        InMemoryEventRepository events = new InMemoryEventRepository();
        InMemoryServicePackageRepository packages = new InMemoryServicePackageRepository();
        EventPlanningFacade planning = new EventPlanningFacade(new EventCatalog(events), new PackageCatalog(packages));
        var first = planning.createEvent("Концерт", LocalDate.of(2026, 12, 1), 100, EventType.CONCERT);
        var second = planning.createEvent("Встреча", LocalDate.of(2026, 12, 2), 30, EventType.WORKSHOP);
        BookingEventLog log = new BookingEventLog();
        BookingService service = new BookingService(events, packages, new InMemoryParticipantRepository(),
            new InMemoryBookingRepository(), List.of(log));
        Booking standard = service.create(first.event().id(), first.starterPackage().id(),
            "Алия", "aliya@example.com", PricingMode.STANDARD);
        Booking withFee = service.create(first.event().id(), first.starterPackage().id(),
            "Марат", "marat@example.com", PricingMode.SERVICE_FEE);
        check(standard.totalPriceCents() == 500_000, "standard price");
        check(withFee.totalPriceCents() == 550_000, "service fee price");
        check(service.list().size() == 2, "bookings are listed");
        expectError(() -> service.create(first.event().id(), second.starterPackage().id(),
            "Ошибка", "error@example.com", PricingMode.STANDARD));
        expectError(() -> service.create(first.event().id(), first.starterPackage().id(),
            "Ошибка", "bad-email", PricingMode.STANDARD));
        check(service.list().size() == 2, "invalid bookings are not stored");
        service.changeStatus(standard.id(), BookingStatus.CONFIRMED);
        check(log.entries().size() == 1 && log.entries().get(0).contains("NEW → CONFIRMED"),
            "observer reacts exactly once");
        expectError(() -> service.changeStatus(standard.id(), BookingStatus.CONFIRMED));
        check(log.entries().size() == 1, "invalid transition does not notify");
        service.changeStatus(standard.id(), BookingStatus.CANCELLED);
        check(log.entries().size() == 2 && log.entries().get(1).contains("CONFIRMED → CANCELLED"),
            "observer reacts to cancellation");
        expectError(() -> service.changeStatus(UUID.randomUUID(), BookingStatus.CONFIRMED));
        var tiny = planning.createEvent("Малый зал", LocalDate.of(2026, 12, 3), 1, EventType.WORKSHOP);
        var seatOne = service.create(tiny.event().id(), tiny.starterPackage().id(),
            "Один", "one@example.com", PricingMode.STANDARD);
        var seatTwo = service.create(tiny.event().id(), tiny.starterPackage().id(),
            "Два", "two@example.com", PricingMode.STANDARD);
        service.changeStatus(seatOne.id(), BookingStatus.CONFIRMED);
        expectError(() -> service.changeStatus(seatTwo.id(), BookingStatus.CONFIRMED));
        check(service.list().stream().filter(item -> item.booking().id().equals(seatTwo.id()))
            .findFirst().orElseThrow().booking().status() == BookingStatus.NEW,
            "capacity overflow leaves booking unconfirmed");
        System.out.println("9 BookingService scenarios passed");
    }

    private static void expectError(Runnable action) {
        try {
            action.run();
            throw new AssertionError("expected validation error");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
