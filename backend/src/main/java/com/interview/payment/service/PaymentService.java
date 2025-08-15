package com.interview.payment.service;

import com.interview.payment.domain.Payment;

/**
 * Service interface for payment operations.
 */
public interface PaymentService {

    /**
     * Process payment charge for the given payment entity.
     */
    Payment charge(Payment p);
}
