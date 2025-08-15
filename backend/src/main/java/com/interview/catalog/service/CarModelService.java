package com.interview.catalog.service;

import com.interview.catalog.dto.CarModelCreateDto;
import com.interview.catalog.dto.CarModelResponseDto;
import com.interview.catalog.dto.CarModelUpdateDto;
import com.interview.common.web.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for car model operations.
 */
public interface CarModelService {

    /**
     * Get car model by ID.
     */
    CarModelResponseDto get(Long id);

    /**
     * Create a new car model.
     */
    CarModelResponseDto create(CarModelCreateDto createDto);

    /**
     * Update an existing car model.
     */
    CarModelResponseDto update(CarModelUpdateDto updateDto, Long id);

    /**
     * Delete a car model.
     */
    void delete(Long id);

    /**
     * List car models by manufacturer with pagination.
     */
    PageResponse<CarModelResponseDto> listByMake(String make, Pageable pageable);

    /**
     * Get all car models with pagination.
     */
    PageResponse<CarModelResponseDto> getAllModels(Pageable pageable);
}