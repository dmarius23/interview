package com.interview.company.web;

import com.interview.common.web.PageResponse;
import com.interview.company.dto.RentalCompanyCreateDto;
import com.interview.company.dto.RentalCompanyResponseDto;
import com.interview.company.dto.RentalCompanyUpdateDto;
import com.interview.company.service.RentalCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for rental company operations.
 */
@RestController
@RequestMapping("/api/companies")
@Validated
@RequiredArgsConstructor
public class RentalCompanyController {
    private final RentalCompanyService rentalCompanyService;

    /**
     * Get all rental companies with pagination.
     */
    @GetMapping
    public PageResponse<RentalCompanyResponseDto> getAllCompanies(Pageable pageable) {
        return rentalCompanyService.listCompanies(pageable);
    }

    /**
     * Get rental company by ID.
     */
    @GetMapping("/{id}")
    public RentalCompanyResponseDto getCompany(@PathVariable Long id) {
        return rentalCompanyService.getCompany(id);
    }

    /**
     * Create a new rental company.
     */
    @PostMapping
    public RentalCompanyResponseDto create(@Valid @RequestBody RentalCompanyCreateDto req) {
        return rentalCompanyService.addCompany(req);
    }

    /**
     * Update an existing rental company.
     */
    @PutMapping("/{id}")
    public RentalCompanyResponseDto update(@PathVariable Long id, @Valid @RequestBody RentalCompanyUpdateDto req) {
        return rentalCompanyService.updateCompany(id, req);
    }

    /**
     * Delete a rental company.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        rentalCompanyService.deleteCompanySoft(id);
    }
}