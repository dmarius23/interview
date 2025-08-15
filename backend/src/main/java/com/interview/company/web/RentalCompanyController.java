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

@RestController
@RequestMapping("/api/companies")
@Validated
@RequiredArgsConstructor
public class RentalCompanyController {
    private final RentalCompanyService rentalCompanyService;

    @GetMapping
    public PageResponse<RentalCompanyResponseDto> getAllCompanies(Pageable pageable) {
        return rentalCompanyService.listCompanies(pageable);
    }

    @GetMapping("/{id}")
    public RentalCompanyResponseDto getCompany(@PathVariable Long id) {
        return rentalCompanyService.getCompany(id);
    }

    @PostMapping
    public RentalCompanyResponseDto create(@Valid @RequestBody RentalCompanyCreateDto req) {
        return rentalCompanyService.addCompany(req);
    }

    @PutMapping("/{id}")
    public RentalCompanyResponseDto update(@PathVariable Long id, @Valid @RequestBody RentalCompanyUpdateDto req) {
        return rentalCompanyService.updateCompany(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        rentalCompanyService.deleteCompanySoft(id);
    }
}