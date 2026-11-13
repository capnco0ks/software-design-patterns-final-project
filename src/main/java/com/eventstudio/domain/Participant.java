package com.eventstudio.domain;

import java.util.UUID;

public record Participant(UUID id, String name, String email) {
}
