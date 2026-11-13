package com.eventstudio.application.report;

import com.eventstudio.domain.Booking;
import com.eventstudio.domain.BookingStatus;
import com.eventstudio.domain.Event;
import com.eventstudio.domain.ServicePackage;
import com.eventstudio.domain.report.ReportVisitor;

/** One visitor handles three different domain elements in the final summary. */
public final class SummaryReportVisitor implements ReportVisitor {
    private int eventCount;
    private int packageCount;
    private int bookingCount;
    private int confirmedCount;
    private int cancelledCount;
    private long confirmedRevenueCents;

    @Override
    public void visit(Event event) {
        eventCount++;
    }

    @Override
    public void visit(ServicePackage servicePackage) {
        packageCount++;
    }

    @Override
    public void visit(Booking booking) {
        bookingCount++;
        if (booking.status() == BookingStatus.CONFIRMED) {
            confirmedCount++;
            confirmedRevenueCents = Math.addExact(confirmedRevenueCents, booking.totalPriceCents());
        } else if (booking.status() == BookingStatus.CANCELLED) {
            cancelledCount++;
        }
    }

    public ReportSnapshot snapshot() {
        return new ReportSnapshot(eventCount, packageCount, bookingCount,
            confirmedCount, cancelledCount, confirmedRevenueCents);
    }
}
