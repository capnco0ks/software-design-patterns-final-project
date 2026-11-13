package com.eventstudio.domain;

import java.util.UUID;
import com.eventstudio.domain.report.ReportElement;
import com.eventstudio.domain.report.ReportVisitor;

public record ServicePackage(UUID id, UUID eventId, String name, long basePriceCents) implements ReportElement {
    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }
}
