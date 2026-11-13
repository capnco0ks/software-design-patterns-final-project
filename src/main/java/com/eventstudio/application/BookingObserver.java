package com.eventstudio.application;

import com.eventstudio.domain.Booking;
import com.eventstudio.domain.BookingStatus;

public interface BookingObserver {
    void statusChanged(Booking booking, BookingStatus previousStatus);
}
