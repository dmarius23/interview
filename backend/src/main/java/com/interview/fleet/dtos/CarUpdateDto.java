package com.interview.fleet.dtos;

import com.interview.fleet.domain.CarStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Data
public class CarUpdateDto {
    private Long id;
    @PositiveOrZero
    private Integer mileageKm;
    private Long currentLocationId;
    private CarStatus status;
    @Positive
    private Integer pricePerDay;
}
