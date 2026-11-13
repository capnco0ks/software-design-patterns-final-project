package com.eventstudio.domain.report;

import com.eventstudio.domain.Booking;
import com.eventstudio.domain.Event;
import com.eventstudio.domain.ServicePackage;

public interface ReportVisitor {
    void visit(Event event);
    void visit(ServicePackage servicePackage);
    void visit(Booking booking);
}
