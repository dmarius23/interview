package com.interview.fleet.service;

import com.interview.common.web.PageResponse;
import com.interview.fleet.dtos.CarCreateDto;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.dtos.CarUpdateDto;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;

public interface CarService {

    Optional<CarResponseDto> findById(Long id);

    PageResponse<CarResponseDto> findByCompanyId(Long companyId, Pageable pageable);

    CarResponseDto addCar(CarCreateDto createDto);

    CarResponseDto updateCar(Long carId, CarUpdateDto updateDto);

    void deleteCar(Long carId);

    // Internal methods for booking service - return entities for performance
    com.interview.fleet.domain.Car lockByIdForUpdate(Long id);

    com.interview.fleet.domain.Car lockAndReserveIfAvailableOrThrow(Long id, Instant from, Instant to);
}