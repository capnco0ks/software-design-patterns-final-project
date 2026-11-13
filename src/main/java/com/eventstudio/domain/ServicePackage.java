package com.eventstudio.domain;

import java.util.UUID;

public record ServicePackage(UUID id, UUID eventId, String name, long basePriceCents) {
}
