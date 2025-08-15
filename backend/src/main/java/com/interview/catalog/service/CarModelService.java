package com.interview.catalog.service;

import com.interview.catalog.dto.CarModelCreateDto;
import com.interview.catalog.dto.CarModelResponseDto;
import com.interview.catalog.dto.CarModelUpdateDto;
import com.interview.common.web.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CarModelService {
    CarModelResponseDto get(Long id);

    CarModelResponseDto create(CarModelCreateDto createDto);

    CarModelResponseDto update(CarModelUpdateDto updateDto, Long id);

    void delete(Long id);

    PageResponse<CarModelResponseDto> listByMake(String make, Pageable pageable);

    PageResponse<CarModelResponseDto> getAllModels(Pageable pageable);
}
