package com.interview.booking.service.impl;

import com.interview.booking.service.DiscountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DiscountServiceImpl implements DiscountService {
    // Demonstration of a nested transaction
    @Transactional(propagation = Propagation.NESTED)
    public int applyPromotions(Long clientId, int dailyPriceInCents, String coupon) {
        // code for calculating discount
        // return discounted price
        return dailyPriceInCents;
    }
}
