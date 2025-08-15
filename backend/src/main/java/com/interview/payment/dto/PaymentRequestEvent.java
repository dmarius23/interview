package com.interview.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestEvent {
    private Long bookingId;
    private Long clientId;
    private Integer amountCents;
    private String currency = "USD";
    private LocalDateTime requestedAt;
}
