package com.eventstudio.application.pricing;

public final class ServiceFeePricing implements PricingStrategy {
    @Override
    public long calculate(long basePriceCents) {
        return Math.addExact(basePriceCents, Math.floorDiv(Math.addExact(basePriceCents, 9), 10));
    }
}
