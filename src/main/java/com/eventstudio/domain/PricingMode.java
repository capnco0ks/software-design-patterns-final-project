package com.eventstudio.domain;

public enum PricingMode {
    STANDARD("Без сервисного сбора"),
    SERVICE_FEE("Сервисный сбор 10%");

    private final String label;

    PricingMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
