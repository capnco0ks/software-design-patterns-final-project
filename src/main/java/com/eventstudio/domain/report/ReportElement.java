package com.eventstudio.domain.report;

public interface ReportElement {
    void accept(ReportVisitor visitor);
}
