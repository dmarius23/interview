package com.interview.fleet.service;

import com.interview.common.web.PageResponse;
import com.interview.fleet.dtos.CarCreateDto;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.dtos.CarUpdateDto;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;

/**
 * Service interface for car management operations.
 */
public interface CarService {

    /**
     * Find car by ID.
     */
    Optional<CarResponseDto> findById(Long id);

    /**
     * Find all cars belonging to a specific company with pagination.
     */
    PageResponse<CarResponseDto> findByCompanyId(Long companyId, Pageable pageable);

    /**
     * Add a new car to the system.
     */
    CarResponseDto addCar(CarCreateDto createDto);

    /**
     * Update an existing car's details.
     */
    CarResponseDto updateCar(Long carId, CarUpdateDto updateDto);

    /**
     * Delete a car from the system.
     */
    void deleteCar(Long carId);

    /**
     * Lock car by ID for update operations (internal use for booking service).
     */
    com.interview.fleet.domain.Car lockByIdForUpdate(Long id);

    /**
     * Lock and reserve car if available for the specified time period.
     */
    com.interview.fleet.domain.Car lockAndReserveIfAvailableOrThrow(Long id, Instant from, Instant to);
}