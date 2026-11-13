package com.eventstudio.domain;

import java.time.LocalDate;
import java.util.UUID;
import com.eventstudio.domain.report.ReportElement;
import com.eventstudio.domain.report.ReportVisitor;

public sealed interface Event extends ReportElement permits WorkshopEvent, ConcertEvent {
    UUID id();

    String title();

    LocalDate date();

    int capacity();

    EventType type();

    @Override
    default void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }
}
