package com.interview.booking.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@Data
public class BookingCreateByCarDto {
    @NotNull
    private Long clientId;
    @NotNull
    private Long carId;
    @NotNull
    private Long pickupLocationId;
    @NotNull
    private Long returnLocationId;
    @NotNull
    private Instant pickup;
    @NotNull
    private Instant ret;
    @Size(max = 40)
    private String coupon;
}
