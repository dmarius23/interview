package com.interview.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseEvent {
    private Long bookingId;
    private boolean success;
    private String transactionId;
    private String errorMessage;
    private LocalDateTime processedAt;
}
