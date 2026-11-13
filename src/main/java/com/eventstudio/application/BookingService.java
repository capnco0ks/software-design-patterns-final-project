package com.eventstudio.application;

import com.eventstudio.application.pricing.PricingStrategy;
import com.eventstudio.application.pricing.ServiceFeePricing;
import com.eventstudio.application.pricing.StandardPricing;
import com.eventstudio.domain.Booking;
import com.eventstudio.domain.BookingStatus;
import com.eventstudio.domain.Event;
import com.eventstudio.domain.Participant;
import com.eventstudio.domain.PricingMode;
import com.eventstudio.domain.ServicePackage;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Booking rules; web code only parses input and renders results. */
public final class BookingService {
    private final EventRepository events;
    private final ServicePackageRepository packages;
    private final ParticipantRepository participants;
    private final BookingRepository bookings;
    private final Map<PricingMode, PricingStrategy> pricing;
    private final List<BookingObserver> observers;

    public BookingService(EventRepository events, ServicePackageRepository packages,
                          ParticipantRepository participants, BookingRepository bookings,
                          List<BookingObserver> observers) {
        this.events = events;
        this.packages = packages;
        this.participants = participants;
        this.bookings = bookings;
        this.observers = List.copyOf(observers);
        this.pricing = Map.of(PricingMode.STANDARD, new StandardPricing(),
            PricingMode.SERVICE_FEE, new ServiceFeePricing());
    }

    public Booking create(UUID eventId, UUID packageId, String name, String email, PricingMode mode) {
        long total = quote(eventId, packageId, name, email, mode);
        Participant participant = new Participant(UUID.randomUUID(), name.strip(), email.strip());
        Booking booking = new Booking(UUID.randomUUID(), eventId, packageId, participant.id(),
            BookingStatus.NEW, mode, total);
        participants.save(participant);
        bookings.save(booking);
        return booking;
    }

    /** Validates a request and calculates the price without changing stored data. */
    public long quote(UUID eventId, UUID packageId, String name, String email, PricingMode mode) {
        Event event = events.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));
        ServicePackage servicePackage = packages.findByEventId(event.id()).stream()
            .filter(item -> item.id().equals(packageId)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Пакет не принадлежит мероприятию"));
        if (name == null || name.isBlank() || name.length() > 120) {
            throw new IllegalArgumentException("Введите имя участника до 120 символов");
        }
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || email.length() > 200) {
            throw new IllegalArgumentException("Введите корректный email");
        }
        PricingStrategy strategy = mode == null ? null : pricing.get(mode);
        if (strategy == null) {
            throw new IllegalArgumentException("Выберите способ расчёта");
        }
        if (servicePackage.basePriceCents() < 0) {
            throw new IllegalArgumentException("Цена пакета не может быть отрицательной");
        }
        return strategy.calculate(servicePackage.basePriceCents());
    }

    public Booking changeStatus(UUID id, BookingStatus next) {
        Booking current = bookings.findBookingById(id).orElseThrow(() -> new IllegalArgumentException("Бронь не найдена"));
        if (next == null || next == BookingStatus.NEW || current.status() == next
            || current.status() == BookingStatus.CANCELLED
            || (current.status() == BookingStatus.CONFIRMED && next != BookingStatus.CANCELLED)) {
            throw new IllegalArgumentException("Недопустимый переход статуса брони");
        }
        if (next == BookingStatus.CONFIRMED) {
            Event event = events.findById(current.eventId())
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено"));
            long occupied = bookings.findAllBookings().stream()
                .filter(item -> item.eventId().equals(current.eventId())
                    && item.status() == BookingStatus.CONFIRMED).count();
            if (occupied >= event.capacity()) {
                throw new IllegalArgumentException("На мероприятии больше нет свободных мест");
            }
        }
        Booking changed = new Booking(current.id(), current.eventId(), current.packageId(),
            current.participantId(), next, current.pricingMode(), current.totalPriceCents());
        bookings.save(changed);
        for (BookingObserver observer : observers) {
            observer.statusChanged(changed, current.status());
        }
        return changed;
    }

    public List<BookingOverview> list() {
        return bookings.findAllBookings().stream().map(booking -> new BookingOverview(booking,
            events.findById(booking.eventId()).orElseThrow(),
            packages.findByEventId(booking.eventId()).stream()
                .filter(item -> item.id().equals(booking.packageId())).findFirst().orElseThrow(),
            participants.findParticipantById(booking.participantId()).orElseThrow())).toList();
    }
}
