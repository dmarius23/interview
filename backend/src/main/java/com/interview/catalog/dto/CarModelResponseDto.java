package com.interview.catalog.dto;

import lombok.Data;

@Data
public class CarModelResponseDto {
    private Long id;
    private String make;
    private String model;
    private String vehicleClass;
    private Integer seats;
}
