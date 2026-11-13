package com.eventstudio.application;

import com.eventstudio.domain.Booking;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    List<Booking> findAll();
    Optional<Booking> findById(UUID id);
    void save(Booking booking);
}
