package com.eventstudio.application.pricing;

public interface PricingStrategy {
    long calculate(long basePriceCents);
}
