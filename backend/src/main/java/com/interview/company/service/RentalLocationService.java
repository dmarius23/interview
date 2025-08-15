package com.interview.company.service;

import com.interview.common.web.PageResponse;
import com.interview.company.domain.RentalLocation;
import com.interview.company.dto.RentalLocationCreateDto;
import com.interview.company.dto.RentalLocationResponseDto;
import com.interview.company.dto.RentalLocationUpdateDto;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for rental location operations.
 */
public interface RentalLocationService {

    /**
     * Add a new rental location.
     */
    RentalLocationResponseDto addRentalLocation(RentalLocationCreateDto createDto);

    /**
     * Update an existing rental location.
     */
    RentalLocationResponseDto updateRentalLocation(Long id, RentalLocationUpdateDto updateDto);

    /**
     * Soft delete a rental location.
     */
    void deleteRentalLocationSoft(Long id);

    /**
     * List rental locations by city with pagination.
     */
    PageResponse<RentalLocationResponseDto> listLocationsByCity(String city, Pageable pageable);

    /**
     * List rental locations by company ID with pagination.
     */
    PageResponse<RentalLocationResponseDto> listLocationsByCompanyId(Long companyId, Pageable pageable);

    /**
     * Get rental location by ID.
     */
    RentalLocationResponseDto getById(Long id);

    /**
     * Get rental location entity by ID (internal use).
     */
    RentalLocation getEntityById(Long id);
}