package com.eventstudio.application;

import com.eventstudio.domain.Event;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    List<Event> findAll();

    Optional<Event> findById(UUID id);

    void save(Event event);
}
