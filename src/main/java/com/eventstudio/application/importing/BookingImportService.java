package com.eventstudio.application.importing;

import com.eventstudio.application.BookingService;
import java.util.List;

public final class BookingImportService {
    private final BookingImportAdapter adapter;
    private final BookingService bookings;

    public BookingImportService(BookingImportAdapter adapter, BookingService bookings) {
        this.adapter = adapter;
        this.bookings = bookings;
    }

    public int importText(String csv) {
        List<BookingImportRow> rows = adapter.parse(csv);
        // Check every row before creating any booking.
        for (BookingImportRow row : rows) {
            bookings.quote(row.eventId(), row.packageId(), row.name(), row.email(), row.pricingMode());
        }
        for (BookingImportRow row : rows) {
            bookings.create(row.eventId(), row.packageId(), row.name(), row.email(), row.pricingMode());
        }
        return rows.size();
    }
}
