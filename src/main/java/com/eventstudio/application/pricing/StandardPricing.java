package com.eventstudio.application.pricing;

public final class StandardPricing implements PricingStrategy {
    @Override
    public long calculate(long basePriceCents) {
        return basePriceCents;
    }
}
