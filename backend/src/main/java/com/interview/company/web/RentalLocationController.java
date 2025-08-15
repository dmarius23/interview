package com.interview.company.web;

import com.interview.common.web.PageResponse;
import com.interview.company.dto.RentalLocationCreateDto;
import com.interview.company.dto.RentalLocationResponseDto;
import com.interview.company.dto.RentalLocationUpdateDto;
import com.interview.company.service.RentalLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for rental location operations.
 */
@RestController
@RequestMapping("/api/locations")
@Validated
@RequiredArgsConstructor
public class RentalLocationController {
    private final RentalLocationService rentalLocationService;

    /**
     * Create a new rental location.
     */
    @PostMapping
    public RentalLocationResponseDto create(@Valid @RequestBody RentalLocationCreateDto req) {
        return rentalLocationService.addRentalLocation(req);
    }

    /**
     * Update an existing rental location.
     */
    @PutMapping("/{id}")
    public RentalLocationResponseDto update(@PathVariable Long id, @Valid @RequestBody RentalLocationUpdateDto req) {
        return rentalLocationService.updateRentalLocation(id, req);
    }

    /**
     * Delete a rental location.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        rentalLocationService.deleteRentalLocationSoft(id);
    }

    /**
     * Get rental location by ID.
     */
    @GetMapping("/{id}")
    public RentalLocationResponseDto getById(@PathVariable Long id) {
        return rentalLocationService.getById(id);
    }

    /**
     * Get rental locations by city.
     */
    @GetMapping("/byCity")
    public PageResponse<RentalLocationResponseDto> getLocationsByCity(@RequestParam String city, Pageable pageable) {
        return rentalLocationService.listLocationsByCity(city, pageable);
    }

    /**
     * Get rental locations by company.
     */
    @GetMapping("/byCompany")
    public PageResponse<RentalLocationResponseDto> getLocationsByCompany(@RequestParam Long companyId, Pageable pageable) {
        return rentalLocationService.listLocationsByCompanyId(companyId, pageable);
    }
}