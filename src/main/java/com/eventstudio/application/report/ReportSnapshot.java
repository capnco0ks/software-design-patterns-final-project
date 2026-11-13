package com.eventstudio.application.report;

public record ReportSnapshot(int events, int packages, int bookings, int confirmed,
                             int cancelled, long confirmedRevenueCents) {
}
