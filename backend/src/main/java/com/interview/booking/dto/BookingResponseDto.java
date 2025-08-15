package com.interview.booking.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class BookingResponseDto {
    private Long id;
    private String status;
    private Integer totalPriceCents;
    private Long carModelId;
    private Long clientId;
    private Long carId;
    private Long pickupLocationId;
    private Long returnLocationId;
    private Instant pickup;
    private Instant ret;
}
