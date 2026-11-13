package com.eventstudio.application;

import com.eventstudio.domain.Booking;
import com.eventstudio.domain.Event;
import com.eventstudio.domain.Participant;
import com.eventstudio.domain.ServicePackage;

public record BookingOverview(Booking booking, Event event, ServicePackage servicePackage, Participant participant) {
}
