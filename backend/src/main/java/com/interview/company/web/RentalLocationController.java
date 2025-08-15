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

@RestController
@RequestMapping("/api/locations")
@Validated
@RequiredArgsConstructor
public class RentalLocationController {
    private final RentalLocationService rentalLocationService;

    @PostMapping
    public RentalLocationResponseDto create(@Valid @RequestBody RentalLocationCreateDto req) {
        return rentalLocationService.addRentalLocation(req);
    }

    @PutMapping("/{id}")
    public RentalLocationResponseDto update(@PathVariable Long id, @Valid @RequestBody RentalLocationUpdateDto req) {
        return rentalLocationService.updateRentalLocation(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        rentalLocationService.deleteRentalLocationSoft(id);
    }

    @GetMapping("/{id}")
    public RentalLocationResponseDto getById(@PathVariable Long id) {
        return rentalLocationService.getById(id);
    }

    @GetMapping("/byCity")
    public PageResponse<RentalLocationResponseDto> getLocationsByCity(@RequestParam String city, Pageable pageable) {
        return rentalLocationService.listLocationsByCity(city, pageable);
    }

    @GetMapping("/byCompany")
    public PageResponse<RentalLocationResponseDto> getLocationsByCompany(@RequestParam Long companyId, Pageable pageable) {
        return rentalLocationService.listLocationsByCompanyId(companyId, pageable);
    }
}