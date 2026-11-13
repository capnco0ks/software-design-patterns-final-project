package com.eventstudio.application;

import com.eventstudio.domain.Participant;
import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository {
    Optional<Participant> findParticipantById(UUID id);
    void save(Participant participant);
}
