package com.eventstudio.storage;

import com.eventstudio.application.BookingRepository;
import com.eventstudio.domain.Booking;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryBookingRepository implements BookingRepository {
    private final Map<UUID, Booking> bookings = new LinkedHashMap<>();

    public synchronized List<Booking> findAllBookings() {
        return new ArrayList<>(bookings.values());
    }

    public synchronized Optional<Booking> findBookingById(UUID id) {
        return Optional.ofNullable(bookings.get(id));
    }

    public synchronized void save(Booking booking) {
        bookings.put(booking.id(), booking);
    }
}
