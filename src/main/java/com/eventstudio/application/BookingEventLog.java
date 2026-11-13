package com.eventstudio.application;

import com.eventstudio.domain.Booking;
import com.eventstudio.domain.BookingStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Observer whose reactions are visible in the booking activity panel. */
public final class BookingEventLog implements BookingObserver {
    private final List<String> entries = new ArrayList<>();
    private final Consumer<String> persist;

    public BookingEventLog() {
        this(List.of(), ignored -> { });
    }

    public BookingEventLog(List<String> initialEntries, Consumer<String> persist) {
        entries.addAll(initialEntries);
        this.persist = persist;
    }

    @Override
    public synchronized void statusChanged(Booking booking, BookingStatus previousStatus) {
        String entry = "Бронь " + booking.id() + ": " + previousStatus + " → " + booking.status();
        persist.accept(entry);
        entries.add(entry);
    }

    public synchronized List<String> entries() {
        return List.copyOf(entries);
    }
}
