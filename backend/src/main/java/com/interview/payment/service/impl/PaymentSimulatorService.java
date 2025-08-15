package com.interview.payment.service.impl;

import com.interview.payment.dto.PaymentRequestEvent;
import com.interview.payment.dto.PaymentResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSimulatorService {

    private final Random random = new Random();

    /**
     * Simulate payment processing with random success/failure
     */
    public PaymentResponseEvent processPayment(PaymentRequestEvent paymentRequest) {
        log.info("Processing payment for booking: {} amount: {}",
                paymentRequest.getBookingId(), paymentRequest.getAmountCents());

        // Simulate processing delay
        try {
            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean success = random.nextDouble() < 0.6;

        PaymentResponseEvent response = new PaymentResponseEvent();
        response.setBookingId(paymentRequest.getBookingId());
        response.setSuccess(success);
        response.setProcessedAt(LocalDateTime.now());

        if (success) {
            response.setTransactionId("TXN-" + System.currentTimeMillis());
            log.info("Payment successful for booking: {} transaction: {}",
                    paymentRequest.getBookingId(), response.getTransactionId());
        } else {
            response.setErrorMessage("Payment declined by bank");
            log.warn("Payment failed for booking: {}", paymentRequest.getBookingId());
        }

        return response;
    }
}
