package com.interview.fleet.dtos;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CarCreateDto {
    @NotNull
    private Long companyId;
    @NotNull
    private Long modelId;
    private Long currentLocationId;
    @NotBlank
    @Size(max = 40)
    private String vin;
    @NotBlank
    @Size(max = 20)
    private String plateNumber;
    @PositiveOrZero
    private Integer mileageKm;
    @Positive
    @NotNull
    private Integer pricePerDay; // cents
}
