package com.interview.payment.service;

import com.interview.payment.domain.Payment;

public interface PaymentService {
    Payment charge(Payment p);
}
