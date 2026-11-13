package com.eventstudio.domain;

import java.util.UUID;
import com.eventstudio.domain.report.ReportElement;
import com.eventstudio.domain.report.ReportVisitor;

public record Booking(UUID id, UUID eventId, UUID packageId, UUID participantId,
                      BookingStatus status, PricingMode pricingMode, long totalPriceCents) implements ReportElement {
    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }
}
