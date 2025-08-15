package com.interview.fleet.service;

import com.interview.catalog.dto.CarModelResponseDto;
import com.interview.common.web.PageResponse;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.repo.dto.CarInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface CarAvailabilityService {

    Page<CarInfoDto> searchAvailable(Long locationId, Instant from, Instant to, Pageable pageable);

    PageResponse<CarModelResponseDto> availableModelsAtLocationForCompany(
            Long companyId, Long locationId, Instant from, Instant to, Pageable pageable);

    PageResponse<CarModelResponseDto> availableModelsInCityForCompany(
            Long companyId, String city, Instant from, Instant to, Pageable pageable);

    PageResponse<CarResponseDto> getAvailableCarsByModelAtLocation(
            Long modelId, Long locationId, Instant from, Instant to, Pageable pageable);

    PageResponse<CarResponseDto> findAvailableCarsByCity(
            String city, Instant from, Instant to, Pageable pageable);

    // Internal method for booking service - returns IDs for performance
    List<Long> findAvailableCarIds(Long modelId, Long locationId, Instant from, Instant to, Pageable limit);

    boolean isCarAvailable(Long carId, Instant from, Instant to);
}
