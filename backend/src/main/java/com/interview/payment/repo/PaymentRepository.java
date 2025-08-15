package com.interview.payment.repo;

import com.interview.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

// PaymentRepository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}

