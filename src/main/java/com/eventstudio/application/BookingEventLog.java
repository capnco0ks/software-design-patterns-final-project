package com.eventstudio.application;

import com.eventstudio.domain.Booking;
import com.eventstudio.domain.BookingStatus;
import java.util.ArrayList;
import java.util.List;

/** Observer whose reactions are visible in the booking activity panel. */
public final class BookingEventLog implements BookingObserver {
    private final List<String> entries = new ArrayList<>();

    @Override
    public synchronized void statusChanged(Booking booking, BookingStatus previousStatus) {
        entries.add("Бронь " + booking.id() + ": " + previousStatus + " → " + booking.status());
    }

    public synchronized List<String> entries() {
        return List.copyOf(entries);
    }
}
