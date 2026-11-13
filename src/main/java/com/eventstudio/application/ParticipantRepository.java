package com.eventstudio.application;

import com.eventstudio.domain.Participant;
import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository {
    Optional<Participant> findById(UUID id);
    void save(Participant participant);
}
