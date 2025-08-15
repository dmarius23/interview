package com.interview.company.service;


import com.interview.common.web.PageResponse;
import com.interview.company.domain.RentalLocation;
import com.interview.company.dto.RentalLocationCreateDto;
import com.interview.company.dto.RentalLocationResponseDto;
import com.interview.company.dto.RentalLocationUpdateDto;
import org.springframework.data.domain.Pageable;

public interface RentalLocationService {

    RentalLocationResponseDto addRentalLocation(RentalLocationCreateDto createDto);

    RentalLocationResponseDto updateRentalLocation(Long id, RentalLocationUpdateDto updateDto);

    void deleteRentalLocationSoft(Long id);

    PageResponse<RentalLocationResponseDto> listLocationsByCity(String city, Pageable pageable);

    PageResponse<RentalLocationResponseDto> listLocationsByCompanyId(Long companyId, Pageable pageable);

    RentalLocationResponseDto getById(Long id);

    RentalLocation getEntityById(Long id);
}
