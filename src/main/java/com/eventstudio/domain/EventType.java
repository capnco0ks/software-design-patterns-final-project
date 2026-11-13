package com.eventstudio.domain;

public enum EventType {
    WORKSHOP("Мастер-класс"),
    CONCERT("Концерт");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
