package com.eventstudio.storage;

import com.eventstudio.application.EventRepository;
import com.eventstudio.domain.Event;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Temporary storage for the first runnable screen; persistent storage is still required. */
public final class InMemoryEventRepository implements EventRepository {
    private final Map<UUID, Event> events = new LinkedHashMap<>();

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
    }
}
