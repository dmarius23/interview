package com.interview.company.service;

import com.interview.common.web.PageResponse;
import com.interview.company.domain.RentalCompany;
import com.interview.company.dto.RentalCompanyCreateDto;
import com.interview.company.dto.RentalCompanyResponseDto;
import com.interview.company.dto.RentalCompanyUpdateDto;
import org.springframework.data.domain.Pageable;

import java.util.function.Consumer;

/**
 * Service interface for rental company operations.
 */
public interface RentalCompanyService {

    /**
     * Add a new rental company.
     */
    public RentalCompanyResponseDto addCompany(RentalCompanyCreateDto createDto);

    /**
     * Update an existing rental company.
     */
    public RentalCompanyResponseDto updateCompany(Long companyId, RentalCompanyUpdateDto updateDto);

    /**
     * Soft delete a rental company.
     */
    public void deleteCompanySoft(Long companyId);

    /**
     * List all rental companies with pagination.
     */
    public PageResponse<RentalCompanyResponseDto> listCompanies(Pageable pageable);

    /**
     * Get rental company by ID.
     */
    public RentalCompanyResponseDto getCompany(Long id);

    /**
     * Update rental company using a mutator function (legacy method).
     */
    public RentalCompany updateCompany(Long companyId, Consumer<RentalCompany> mutator);
}