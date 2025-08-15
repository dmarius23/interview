package com.interview.fleet.dtos;

import com.interview.fleet.domain.CarStatus;
import lombok.Data;

@Data
public class CarResponseDto {
    private Long id;
    private String plateNumber;
    private String vin;
    private Integer mileageKm;
    private Integer pricePerDay;
    private CarStatus status;
    private Long companyId;
    private Long modelId;
    private Long currentLocationId;
}
