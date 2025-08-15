package com.interview.fleet.service;

import com.interview.catalog.dto.CarModelResponseDto;
import com.interview.common.web.PageResponse;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.repo.dto.CarInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for car availability operations.
 */
public interface CarAvailabilityService {

    /**
     * Search for available cars at a location within a time window.
     */
    Page<CarInfoDto> searchAvailable(Long locationId, Instant from, Instant to, Pageable pageable);

    /**
     * Find available car models at a specific location for a company.
     */
    PageResponse<CarModelResponseDto> availableModelsAtLocationForCompany(
            Long companyId, Long locationId, Instant from, Instant to, Pageable pageable);

    /**
     * Find available car models in a city for a company.
     */
    PageResponse<CarModelResponseDto> availableModelsInCityForCompany(
            Long companyId, String city, Instant from, Instant to, Pageable pageable);

    /**
     * Get available cars by model at a specific location.
     */
    PageResponse<CarResponseDto> getAvailableCarsByModelAtLocation(
            Long modelId, Long locationId, Instant from, Instant to, Pageable pageable);

    /**
     * Find available cars in a specific city.
     */
    PageResponse<CarResponseDto> findAvailableCarsByCity(
            String city, Instant from, Instant to, Pageable pageable);

    /**
     * Find available car IDs for booking operations (internal use).
     */
    List<Long> findAvailableCarIds(Long modelId, Long locationId, Instant from, Instant to, Pageable limit);

    /**
     * Check if a specific car is available for the given time period.
     */
    boolean isCarAvailable(Long carId, Instant from, Instant to);
}