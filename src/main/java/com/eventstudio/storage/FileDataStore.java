package com.eventstudio.storage;

import com.eventstudio.application.BookingRepository;
import com.eventstudio.application.EventRepository;
import com.eventstudio.application.ParticipantRepository;
import com.eventstudio.application.ServicePackageRepository;
import com.eventstudio.domain.Booking;
import com.eventstudio.domain.BookingStatus;
import com.eventstudio.domain.ConcertEvent;
import com.eventstudio.domain.Event;
import com.eventstudio.domain.EventType;
import com.eventstudio.domain.Participant;
import com.eventstudio.domain.PricingMode;
import com.eventstudio.domain.ServicePackage;
import com.eventstudio.domain.WorkshopEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** One versioned local snapshot for all related entities. */
public final class FileDataStore implements EventRepository, ServicePackageRepository,
                                        ParticipantRepository, BookingRepository {
    private static final String MAGIC = "EVENT_STUDIO_1";
    private static final int MAX_ITEMS = 100_000;
    private final Path path;
    private final Map<UUID, Event> events = new LinkedHashMap<>();
    private final Map<UUID, ServicePackage> packages = new LinkedHashMap<>();
    private final Map<UUID, Participant> participants = new LinkedHashMap<>();
    private final Map<UUID, Booking> bookings = new LinkedHashMap<>();
    private final List<String> log = new ArrayList<>();

    public FileDataStore(Path path) {
        this.path = path.toAbsolutePath();
        read();
    }

    @Override
    public synchronized List<Event> findAll() {
        return new ArrayList<>(events.values());
    }

    @Override
    public synchronized Optional<Event> findById(UUID id) {
        return Optional.ofNullable(events.get(id));
    }

    @Override
    public synchronized void save(Event event) {
        events.put(event.id(), event);
        write();
    }

    @Override
    public synchronized List<ServicePackage> findByEventId(UUID eventId) {
        return packages.values().stream().filter(item -> item.eventId().equals(eventId)).toList();
    }

    @Override
    public synchronized void save(ServicePackage servicePackage) {
        packages.put(servicePackage.id(), servicePackage);
        write();
    }

    @Override
    public synchronized Optional<Participant> findParticipantById(UUID id) {
        return Optional.ofNullable(participants.get(id));
    }

    @Override
    public synchronized void save(Participant participant) {
        participants.put(participant.id(), participant);
        write();
    }

    @Override
    public synchronized List<Booking> findAllBookings() {
        return new ArrayList<>(bookings.values());
    }

    @Override
    public synchronized Optional<Booking> findBookingById(UUID id) {
        return Optional.ofNullable(bookings.get(id));
    }

    @Override
    public synchronized void save(Booking booking) {
        bookings.put(booking.id(), booking);
        write();
    }

    public synchronized List<String> bookingEvents() {
        return List.copyOf(log);
    }

    public synchronized void appendBookingEvent(String entry) {
        log.add(entry);
        write();
    }

    private void read() {
        if (!Files.exists(path)) {
            return;
        }
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            if (!MAGIC.equals(input.readUTF())) {
                throw new IOException("Неизвестный формат данных");
            }
            for (int i = count(input); i > 0; i--) {
                EventType type = EventType.valueOf(input.readUTF());
                UUID id = UUID.fromString(input.readUTF());
                String title = input.readUTF();
                LocalDate date = LocalDate.parse(input.readUTF());
                int capacity = input.readInt();
                Event event = type == EventType.WORKSHOP
                    ? new WorkshopEvent(id, title, date, capacity)
                    : new ConcertEvent(id, title, date, capacity);
                events.put(id, event);
            }
            for (int i = count(input); i > 0; i--) {
                ServicePackage item = new ServicePackage(UUID.fromString(input.readUTF()),
                    UUID.fromString(input.readUTF()), input.readUTF(), input.readLong());
                packages.put(item.id(), item);
            }
            for (int i = count(input); i > 0; i--) {
                Participant item = new Participant(UUID.fromString(input.readUTF()), input.readUTF(), input.readUTF());
                participants.put(item.id(), item);
            }
            for (int i = count(input); i > 0; i--) {
                Booking item = new Booking(UUID.fromString(input.readUTF()), UUID.fromString(input.readUTF()),
                    UUID.fromString(input.readUTF()), UUID.fromString(input.readUTF()),
                    BookingStatus.valueOf(input.readUTF()), PricingMode.valueOf(input.readUTF()), input.readLong());
                bookings.put(item.id(), item);
            }
            for (int i = count(input); i > 0; i--) {
                log.add(input.readUTF());
            }
            if (input.read() != -1) {
                throw new IOException("Лишние данные после окончания файла");
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Не удалось прочитать " + path + ": " + exception.getMessage(), exception);
        }
    }

    private static int count(DataInputStream input) throws IOException {
        int value = input.readInt();
        if (value < 0 || value > MAX_ITEMS) {
            throw new IOException("Недопустимое количество записей");
        }
        return value;
    }

    private void write() {
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temporary)))) {
                output.writeUTF(MAGIC);
                output.writeInt(events.size());
                for (Event event : events.values()) {
                    output.writeUTF(event.type().name());
                    output.writeUTF(event.id().toString());
                    output.writeUTF(event.title());
                    output.writeUTF(event.date().toString());
                    output.writeInt(event.capacity());
                }
                output.writeInt(packages.size());
                for (ServicePackage item : packages.values()) {
                    output.writeUTF(item.id().toString());
                    output.writeUTF(item.eventId().toString());
                    output.writeUTF(item.name());
                    output.writeLong(item.basePriceCents());
                }
                output.writeInt(participants.size());
                for (Participant item : participants.values()) {
                    output.writeUTF(item.id().toString());
                    output.writeUTF(item.name());
                    output.writeUTF(item.email());
                }
                output.writeInt(bookings.size());
                for (Booking item : bookings.values()) {
                    output.writeUTF(item.id().toString());
                    output.writeUTF(item.eventId().toString());
                    output.writeUTF(item.packageId().toString());
                    output.writeUTF(item.participantId().toString());
                    output.writeUTF(item.status().name());
                    output.writeUTF(item.pricingMode().name());
                    output.writeLong(item.totalPriceCents());
                }
                output.writeInt(log.size());
                for (String entry : log) {
                    output.writeUTF(entry);
                }
            }
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось сохранить данные: " + exception.getMessage(), exception);
        }
    }
}
