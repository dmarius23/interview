package com.interview.payment.service.impl;

import com.interview.payment.domain.Payment;
import com.interview.payment.domain.PaymentStatus;
import com.interview.payment.repo.PaymentRepository;
import com.interview.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment charge(Payment p) {
        //  payment logic ...
        p.setStatus(PaymentStatus.AUTHORIZED);
        return paymentRepository.save(p);
    }
}
