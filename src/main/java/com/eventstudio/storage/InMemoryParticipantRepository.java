package com.eventstudio.storage;

import com.eventstudio.application.ParticipantRepository;
import com.eventstudio.domain.Participant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryParticipantRepository implements ParticipantRepository {
    private final Map<UUID, Participant> participants = new LinkedHashMap<>();

    public synchronized Optional<Participant> findById(UUID id) {
        return Optional.ofNullable(participants.get(id));
    }

    public synchronized void save(Participant participant) {
        participants.put(participant.id(), participant);
    }
}
