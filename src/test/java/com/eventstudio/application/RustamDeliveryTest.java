package com.eventstudio.application;

import com.eventstudio.application.importing.BookingImportService;
import com.eventstudio.application.importing.CsvBookingAdapter;
import com.eventstudio.application.report.ReportService;
import com.eventstudio.domain.BookingStatus;
import com.eventstudio.domain.EventType;
import com.eventstudio.domain.PricingMode;
import com.eventstudio.storage.FileDataStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public final class RustamDeliveryTest {
    private RustamDeliveryTest() {
    }

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("event-studio-test-");
        Path file = directory.resolve("state.bin");
        FileDataStore store = new FileDataStore(file);
        var planning = new EventPlanningFacade(new EventCatalog(store), new PackageCatalog(store));
        var event = planning.createEvent("Тестовый концерт", LocalDate.of(2026, 12, 1), 30, EventType.CONCERT);
        BookingEventLog log = new BookingEventLog(store.bookingEvents(), store::appendBookingEvent);
        BookingService bookings = new BookingService(store, store, store, store, List.of(log));
        var direct = bookings.create(event.event().id(), event.starterPackage().id(),
            "Алия", "aliya@example.com", PricingMode.SERVICE_FEE);
        bookings.changeStatus(direct.id(), BookingStatus.CONFIRMED);
        check(Files.size(file) > 0, "file snapshot exists");

        FileDataStore reloaded = new FileDataStore(file);
        check(reloaded.findAll().size() == 1, "event survives restart");
        check(reloaded.findByEventId(event.event().id()).size() == 1, "package survives restart");
        check(reloaded.findAllBookings().size() == 1, "booking survives restart");
        check(reloaded.findParticipantById(direct.participantId()).isPresent(), "participant survives restart");
        check(reloaded.bookingEvents().size() == 1, "observer log survives restart");
        var report = new ReportService(reloaded, reloaded, reloaded);
        check(report.snapshot().events() == 1 && report.snapshot().packages() == 1
            && report.snapshot().bookings() == 1 && report.snapshot().confirmed() == 1
            && report.snapshot().confirmedRevenueCents() == 550_000, "visitor totals");

        BookingService reloadedBookings = new BookingService(reloaded, reloaded, reloaded, reloaded, List.of());
        BookingImportService importer = new BookingImportService(new CsvBookingAdapter(), reloadedBookings);
        String header = "event_id,package_id,name,email,pricing_mode\n";
        String row = event.event().id() + "," + event.starterPackage().id()
            + ",\"Иван, младший\",ivan@example.com,STANDARD\n";
        check(importer.importText(header + row) == 1, "CSV adapter imports quoted names");
        check(reloaded.findAllBookings().size() == 2, "imported booking is stored");
        check(reloaded.findAllBookings().get(1).totalPriceCents() == 500_000, "import uses Strategy");
        expectError(() -> importer.importText(header + row + "bad-id,bad-id,Ошибка,bad,STANDARD"));
        check(reloaded.findAllBookings().size() == 2, "invalid batch creates no bookings");
        check(report.exportCsv().contains("bookings,2"), "CSV report exports visitor totals");
        Files.deleteIfExists(file);
        Files.deleteIfExists(directory);
        System.out.println("12 RustamDelivery scenarios passed");
    }

    private static void expectError(Runnable action) {
        try {
            action.run();
            throw new AssertionError("expected import error");
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
