package com.eventstudio.application.report;

import com.eventstudio.application.BookingRepository;
import com.eventstudio.application.EventRepository;
import com.eventstudio.application.ServicePackageRepository;

public final class ReportService {
    private final EventRepository events;
    private final ServicePackageRepository packages;
    private final BookingRepository bookings;

    public ReportService(EventRepository events, ServicePackageRepository packages, BookingRepository bookings) {
        this.events = events;
        this.packages = packages;
        this.bookings = bookings;
    }

    public ReportSnapshot snapshot() {
        SummaryReportVisitor visitor = new SummaryReportVisitor();
        for (var event : events.findAll()) {
            event.accept(visitor);
            for (var servicePackage : packages.findByEventId(event.id())) {
                servicePackage.accept(visitor);
            }
        }
        for (var booking : bookings.findAllBookings()) {
            booking.accept(visitor);
        }
        return visitor.snapshot();
    }

    public String exportCsv() {
        ReportSnapshot value = snapshot();
        return "metric,value\n"
            + "events," + value.events() + "\n"
            + "packages," + value.packages() + "\n"
            + "bookings," + value.bookings() + "\n"
            + "confirmed," + value.confirmed() + "\n"
            + "cancelled," + value.cancelled() + "\n"
            + "confirmed_revenue_kzt," + value.confirmedRevenueCents() / 100 + "\n";
    }
}
