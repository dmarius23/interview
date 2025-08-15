package com.interview.booking.repo.dto;

import com.interview.booking.domain.BookingStatus;
import com.interview.payment.domain.PaymentStatus;
import lombok.Value;

import java.time.Instant;

@Value
public class BookingInfoDto {
    Long bookingId;
    BookingStatus status;
    Instant pickupTime;
    Instant returnTime;
    Integer totalPriceCents;

    String carPlate;
    String modelMake;
    String modelModel;

    String pickupLocationName;
    String returnLocationName;

    PaymentStatus paymentStatus;
}

