package com.eventstudio.application;

import com.eventstudio.domain.Booking;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    List<Booking> findAllBookings();
    Optional<Booking> findBookingById(UUID id);
    void save(Booking booking);
}
