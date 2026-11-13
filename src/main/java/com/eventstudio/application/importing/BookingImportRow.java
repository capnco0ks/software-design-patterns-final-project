package com.eventstudio.application.importing;

import com.eventstudio.domain.PricingMode;
import java.util.UUID;

public record BookingImportRow(UUID eventId, UUID packageId, String name, String email, PricingMode pricingMode) {
}
