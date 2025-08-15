package com.interview.fleet.repo.dto;

import com.interview.fleet.domain.CarStatus;
import lombok.Value;

@Value
public class CarInfoDto {
    Long carId;
    String plateNumber;
    CarStatus status;
    String modelMake;
    String modelModel;
    String companyName;
    String locationName;
}
