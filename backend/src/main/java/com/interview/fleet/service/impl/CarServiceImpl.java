package com.interview.fleet.service.impl;

import com.interview.booking.domain.BookingStatus;
import com.interview.common.annotation.Loggable;
import com.interview.common.domain.BusinessRuleViolation;
import com.interview.common.domain.EntityNotFound;
import com.interview.common.mapper.PageResponseMapper;
import com.interview.common.web.PageResponse;
import com.interview.fleet.domain.Car;
import com.interview.fleet.dtos.CarCreateDto;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.dtos.CarUpdateDto;
import com.interview.fleet.mapper.CarMapper;
import com.interview.fleet.repo.CarAvailabilityRepository;
import com.interview.fleet.repo.CarRepository;
import com.interview.fleet.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Loggable(logParams = true, logResult = false)
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarAvailabilityRepository carAvailabilityRepository;
    private final CarMapper carMapper;
    private final PageResponseMapper pageResponseMapper;

    @Transactional(readOnly = true)
    @Override
    public Optional<CarResponseDto> findById(Long id) {
        return carRepository.findById(id)
                .map(carMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<CarResponseDto> findByCompanyId(Long companyId, Pageable pageable) {
        Page<Car> page = carRepository.findByCompanyId(companyId, pageable);
        return pageResponseMapper.toPageResponse(page, carMapper::toResponse);

    }

    @Transactional
    @Override
    public CarResponseDto addCar(CarCreateDto createDto) {
        Car car = carMapper.toEntity(createDto);
        Car saved = carRepository.save(car);
        return carMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public CarResponseDto updateCar(Long carId, CarUpdateDto updateDto) {
        updateDto.setId(carId);

        int attempts = 0, maxAttempts = 3;
        while (true) {
            Car car = carRepository.findById(carId)
                    .orElseThrow(() -> new EntityNotFound("Car not found: " + carId));

            carMapper.updateEntityFromDto(updateDto, car);

            try {
                Car saved = carRepository.saveAndFlush(car);
                return carMapper.toResponse(saved);
            } catch (OptimisticLockingFailureException ex) {
                if (++attempts >= maxAttempts) throw ex;
                try {
                    Thread.sleep(25L * attempts);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Transactional
    @Override
    public void deleteCar(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFound("Car not found: " + carId));


        boolean hasActive = carAvailabilityRepository.hasActiveOrUpcomingForCar(
                carId, Instant.now(), BookingStatus.activeSet());
        if (hasActive) {
            throw new BusinessRuleViolation("Car has active or upcoming bookings");
        }

        carRepository.delete(car);
    }

    // Internal methods for booking service - keep returning entities for performance

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public Car lockByIdForUpdate(Long id) {
        return carRepository.lockByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFound("Car not found for locking: " + id));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public Car lockAndReserveIfAvailableOrThrow(Long id, Instant from, Instant to) {
        Car car = carRepository.lockByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFound("Car not found: " + id));
        if (!carAvailabilityRepository.existsActiveForCarInPeriod(id, from, to, BookingStatus.activeSet())) {
            throw new BusinessRuleViolation("Car is not available");
        }
        //car.setStatus(CarStatus.RESERVED);
        return car;
    }
}