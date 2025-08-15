package com.interview.catalog.web;

import com.interview.catalog.dto.CarModelCreateDto;
import com.interview.catalog.dto.CarModelResponseDto;
import com.interview.catalog.dto.CarModelUpdateDto;
import com.interview.catalog.service.impl.CarModelServiceImpl;
import com.interview.common.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for car model operations.
 */
@RestController
@RequestMapping("/api/carmodels")
@Validated
@RequiredArgsConstructor
public class CarModelController {
    private final CarModelServiceImpl carModelServiceImpl;

    /**
     * Create a new car model.
     */
    @PostMapping
    public CarModelResponseDto create(@Valid @RequestBody CarModelCreateDto req) {
        return carModelServiceImpl.create(req);
    }

    /**
     * Get car model by ID.
     */
    @GetMapping("/{id}")
    public CarModelResponseDto get(@PathVariable Long id) {
        return carModelServiceImpl.get(id);
    }

    /**
     * List car models by manufacturer.
     */
    @GetMapping("/byMake")
    public PageResponse<CarModelResponseDto> listByMake(@RequestParam String make, Pageable pageable) {
        return carModelServiceImpl.listByMake(make, pageable);
    }

    /**
     * Get all car models with pagination.
     */
    @GetMapping
    public PageResponse<CarModelResponseDto> getAllModels(Pageable pageable) {
        return carModelServiceImpl.getAllModels(pageable);
    }

    /**
     * Update an existing car model.
     */
    @PutMapping("/{id}")
    public CarModelResponseDto update(@Valid @RequestBody CarModelUpdateDto req, @PathVariable Long id) {
        return carModelServiceImpl.update(req, id);
    }

    /**
     * Delete a car model.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        carModelServiceImpl.delete(id);
    }
}