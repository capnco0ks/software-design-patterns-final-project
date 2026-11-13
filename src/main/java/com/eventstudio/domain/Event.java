package com.eventstudio.domain;

import java.time.LocalDate;
import java.util.UUID;

public record Event(UUID id, String title, LocalDate date, int capacity, EventType type) {
}
