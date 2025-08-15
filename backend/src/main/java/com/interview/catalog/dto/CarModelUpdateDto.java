package com.interview.catalog.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CarModelUpdateDto {
    @NotBlank
    @Size(max = 80)
    private String make;
    @NotBlank
    @Size(max = 80)
    private String model;
    @Size(max = 40)
    private String vehicleClass;
    @Min(1)
    @Max(9)
    private Integer seats;
    @PositiveOrZero
    private Long version;
}
