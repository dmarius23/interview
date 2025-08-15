package com.interview.booking.service;

/**
 * Service interface for discount.
 */
public interface DiscountService {
    int applyPromotions(Long clientId, int dailyPriceInCents, String coupon);
}
