package com.interview.company.service;

import com.interview.common.web.PageResponse;
import com.interview.company.domain.RentalCompany;
import com.interview.company.dto.RentalCompanyCreateDto;
import com.interview.company.dto.RentalCompanyResponseDto;
import com.interview.company.dto.RentalCompanyUpdateDto;
import org.springframework.data.domain.Pageable;

import java.util.function.Consumer;

public interface RentalCompanyService {
    public RentalCompanyResponseDto addCompany(RentalCompanyCreateDto createDto);

    public RentalCompanyResponseDto updateCompany(Long companyId, RentalCompanyUpdateDto updateDto);

    public void deleteCompanySoft(Long companyId);

    public PageResponse<RentalCompanyResponseDto> listCompanies(Pageable pageable);

    public RentalCompanyResponseDto getCompany(Long id);

    public RentalCompany updateCompany(Long companyId, Consumer<RentalCompany> mutator);


}
