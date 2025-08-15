package com.interview.fleet.service.impl;

import com.interview.booking.domain.BookingStatus;
import com.interview.catalog.domain.CarModel;
import com.interview.catalog.dto.CarModelResponseDto;
import com.interview.catalog.mapper.CarModelMapper;
import com.interview.catalog.repo.CarModelRepository;
import com.interview.common.annotation.Loggable;
import com.interview.common.web.PageResponse;
import com.interview.fleet.domain.Car;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.mapper.CarMapper;
import com.interview.fleet.repo.CarAvailabilityRepository;
import com.interview.fleet.repo.dto.CarInfoDto;
import com.interview.fleet.service.CarAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Loggable(logParams = true, logResult = false)
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarAvailabilityServiceImpl implements CarAvailabilityService {

    private final CarAvailabilityRepository carAvailabilityRepository;
    private final CarModelRepository carModelRepository;
    private final CarModelMapper carModelMapper;
    private final CarMapper carMapper;

    @Override
    public Page<CarInfoDto> searchAvailable(Long locationId, Instant from, Instant to, Pageable pageable) {
        return carAvailabilityRepository.findAvailableCars(locationId, from, to, BookingStatus.activeSet(), pageable);
    }

    @Override
    public PageResponse<CarModelResponseDto> availableModelsAtLocationForCompany(
            Long companyId, Long locationId, Instant from, Instant to, Pageable pageable) {

        Page<Long> ids = carAvailabilityRepository.findAvailableModelIdsAtLocationForCompany(
                companyId, locationId, from, to, BookingStatus.activeSet(), pageable);
        List<CarModel> content = carModelRepository.findAllById(ids.getContent());
        Page<CarModel> page = new PageImpl<>(content, pageable, ids.getTotalElements());

        return new PageResponse<>(
                page.map(carModelMapper::toResponse).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public PageResponse<CarModelResponseDto> availableModelsInCityForCompany(
            Long companyId, String city, Instant from, Instant to, Pageable pageable) {

        Page<Long> ids = carAvailabilityRepository.findAvailableModelIdsInCityForCompany(
                companyId, city, from, to, BookingStatus.activeSet(), pageable);
        List<CarModel> content = carModelRepository.findAllById(ids.getContent());
        Page<CarModel> page = new PageImpl<>(content, pageable, ids.getTotalElements());

        return new PageResponse<>(
                page.map(carModelMapper::toResponse).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public PageResponse<CarResponseDto> getAvailableCarsByModelAtLocation(
            Long modelId, Long locationId, Instant from, Instant to, Pageable pageable) {

        Page<Car> page = carAvailabilityRepository.findAvailableCarsByModelAtLocation(
                modelId, locationId, from, to, BookingStatus.activeSet(), pageable);

        return new PageResponse<>(
                page.map(carMapper::toResponse).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public PageResponse<CarResponseDto> findAvailableCarsByCity(
            String city, Instant from, Instant to, Pageable pageable) {

        Page<Car> page = carAvailabilityRepository.findAvailableCarsByCity(
                city, from, to, BookingStatus.activeSet(), pageable);

        return new PageResponse<>(
                page.map(carMapper::toResponse).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    // Internal method for booking service - returns raw IDs for performance
    @Override
    public List<Long> findAvailableCarIds(Long modelId, Long locationId, Instant from, Instant to, Pageable limit) {
        return carAvailabilityRepository.findAvailableCarIds(
                modelId, locationId, from, to, BookingStatus.activeSet(), limit);
    }

    @Override
    public boolean isCarAvailable(Long carId, Instant from, Instant to) {
        return !carAvailabilityRepository.existsActiveForCarInPeriod(carId, from, to, BookingStatus.activeSet());
    }
}