package com.interview.catalog.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CarModelCreateDto {
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
}
