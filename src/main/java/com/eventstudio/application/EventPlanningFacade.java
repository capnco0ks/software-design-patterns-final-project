package com.eventstudio.application;

import com.eventstudio.domain.Event;
import com.eventstudio.domain.EventType;
import com.eventstudio.domain.ServicePackage;
import com.eventstudio.domain.Booking;
import com.eventstudio.domain.BookingStatus;
import com.eventstudio.domain.PricingMode;
import com.eventstudio.application.importing.BookingImportService;
import com.eventstudio.application.report.ReportService;
import com.eventstudio.application.report.ReportSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Facade for the organizer's event planning flow across event and package subsystems. */
public final class EventPlanningFacade {
    private final EventCatalog events;
    private final PackageCatalog packages;
    private final BookingService bookings;
    private final BookingEventLog bookingLog;
    private final BookingImportService importer;
    private final ReportService reports;

    public EventPlanningFacade(EventCatalog events, PackageCatalog packages) {
        this(events, packages, null, null, null, null);
    }

    public EventPlanningFacade(EventCatalog events, PackageCatalog packages,
                               BookingService bookings, BookingEventLog bookingLog) {
        this(events, packages, bookings, bookingLog, null, null);
    }

    public EventPlanningFacade(EventCatalog events, PackageCatalog packages,
                               BookingService bookings, BookingEventLog bookingLog,
                               BookingImportService importer, ReportService reports) {
        this.events = events;
        this.packages = packages;
        this.bookings = bookings;
        this.bookingLog = bookingLog;
        this.importer = importer;
        this.reports = reports;
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

    public Booking createBooking(UUID eventId, UUID packageId, String name, String email, PricingMode mode) {
        return bookings.create(eventId, packageId, name, email, mode);
    }

    public Booking changeBookingStatus(UUID bookingId, BookingStatus status) {
        return bookings.changeStatus(bookingId, status);
    }

    public List<BookingOverview> listBookings() {
        return bookings.list();
    }

    public List<String> bookingEvents() {
        return bookingLog.entries();
    }

    public int importBookings(String csv) {
        return importer.importText(csv);
    }

    public ReportSnapshot report() {
        return reports.snapshot();
    }

    public String exportReportCsv() {
        return reports.exportCsv();
    }

    private EventOverview overviewFor(Event event) {
        ServicePackage starter = packages.listForEvent(event.id()).stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("Пакет услуг не найден для мероприятия " + event.id()));
        return new EventOverview(event, starter);
    }
}
