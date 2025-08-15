package com.interview.fleet;


import com.interview.booking.domain.BookingStatus;
import com.interview.catalog.domain.CarModel;
import com.interview.catalog.repo.CarModelRepository;
import com.interview.common.TestDataFactory;
import com.interview.common.domain.BusinessRuleViolation;
import com.interview.common.domain.EntityNotFound;
import com.interview.common.mapper.PageResponseMapper;
import com.interview.common.web.PageResponse;
import com.interview.company.domain.RentalCompany;
import com.interview.company.domain.RentalLocation;
import com.interview.company.repo.RentalCompanyRepository;
import com.interview.company.repo.RentalLocationRepository;
import com.interview.fleet.domain.Car;
import com.interview.fleet.dtos.CarCreateDto;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.dtos.CarUpdateDto;
import com.interview.fleet.mapper.CarMapper;
import com.interview.fleet.repo.CarAvailabilityRepository;
import com.interview.fleet.repo.CarRepository;
import com.interview.fleet.service.impl.CarServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
        // This prevents unnecessary stubbing errors
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarAvailabilityRepository carAvailabilityRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private RentalCompanyRepository companyRepository;

    @Mock
    private CarModelRepository carModelRepository;

    @Mock
    private RentalLocationRepository locationRepository;

    // Don't mock PageResponseMapper - it's a simple utility class
    private PageResponseMapper pageResponseMapper = new PageResponseMapper();

    @InjectMocks
    private CarServiceImpl carService;

    private Car testCar;
    private CarCreateDto carCreateDto;
    private CarResponseDto carResponseDto;
    private RentalCompany testCompany;
    private CarModel testCarModel;
    private RentalLocation testLocation;

    @BeforeEach
    void setUp() {
        testCompany = TestDataFactory.createTestCompany();
        testCompany.setId(1L);

        testCarModel = TestDataFactory.createTestCarModel();
        testCarModel.setId(1L);

        testLocation = TestDataFactory.createTestLocation(testCompany);
        testLocation.setId(1L);

        testCar = TestDataFactory.createTestCar(testCompany, testCarModel, testLocation);
        testCar.setId(1L);

        carCreateDto = TestDataFactory.createCarCreateDto(1L, 1L, 1L);

        carResponseDto = new CarResponseDto();
        carResponseDto.setId(1L);
        carResponseDto.setPlateNumber(testCar.getPlateNumber());
        carResponseDto.setVin(testCar.getVin());
        carResponseDto.setMileageKm(testCar.getMileageKm());
        carResponseDto.setPricePerDay(testCar.getDailyPriceInCents());
        carResponseDto.setStatus(testCar.getStatus());
        carResponseDto.setCompanyId(1L);
        carResponseDto.setModelId(1L);
        carResponseDto.setCurrentLocationId(1L);

        // Set the real PageResponseMapper in the service
        carService = new CarServiceImpl(
                carRepository,
                carAvailabilityRepository,
                carMapper,
                pageResponseMapper,
                companyRepository,
                carModelRepository,
                locationRepository
        );
    }

    @Test
    void findById_ShouldReturnCar_WhenCarExists() {
        // Given
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(carMapper.toResponse(testCar)).thenReturn(carResponseDto);

        // When
        Optional<CarResponseDto> result = carService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(carRepository).findById(1L);
        verify(carMapper).toResponse(testCar);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenCarNotExists() {
        // Given
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<CarResponseDto> result = carService.findById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(carRepository).findById(1L);
        verifyNoInteractions(carMapper);
    }

    @Test
    void addCar_ShouldCreateCar_WhenValidData() {
        // Given
        when(carMapper.toEntity(carCreateDto)).thenReturn(testCar);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(carModelRepository.findById(1L)).thenReturn(Optional.of(testCarModel));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(carRepository.save(testCar)).thenReturn(testCar);
        when(carMapper.toResponse(testCar)).thenReturn(carResponseDto);

        // When
        CarResponseDto result = carService.addCar(carCreateDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(carMapper).toEntity(carCreateDto);
        verify(companyRepository).findById(1L);
        verify(carModelRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(carRepository).save(testCar);
        verify(carMapper).toResponse(testCar);
    }

    @Test
    void addCar_ShouldThrowException_WhenCompanyNotFound() {
        // Given
        when(carMapper.toEntity(carCreateDto)).thenReturn(testCar);
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carService.addCar(carCreateDto))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Company not found: 1");
    }

    @Test
    void addCar_ShouldThrowException_WhenCarModelNotFound() {
        // Given
        when(carMapper.toEntity(carCreateDto)).thenReturn(testCar);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(carModelRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carService.addCar(carCreateDto))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Car model not found: 1");
    }

    @Test
    void addCar_ShouldThrowException_WhenLocationNotFound() {
        // Given
        when(carMapper.toEntity(carCreateDto)).thenReturn(testCar);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(carModelRepository.findById(1L)).thenReturn(Optional.of(testCarModel));
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carService.addCar(carCreateDto))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Location not found: 1");
    }

    @Test
    void addCar_ShouldSucceed_WhenLocationIsNull() {
        // Given
        carCreateDto.setCurrentLocationId(null);
        when(carMapper.toEntity(carCreateDto)).thenReturn(testCar);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(carModelRepository.findById(1L)).thenReturn(Optional.of(testCarModel));
        when(carRepository.save(testCar)).thenReturn(testCar);
        when(carMapper.toResponse(testCar)).thenReturn(carResponseDto);

        // When
        CarResponseDto result = carService.addCar(carCreateDto);

        // Then
        assertThat(result).isNotNull();
        verify(locationRepository, never()).findById(any());
    }

    @Test
    void updateCar_ShouldUpdateCar_WhenCarExists() {
        // Given
        CarUpdateDto updateDto = new CarUpdateDto();
        updateDto.setId(1L);
        updateDto.setMileageKm(15000);

        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(carRepository.saveAndFlush(testCar)).thenReturn(testCar);
        when(carMapper.toResponse(testCar)).thenReturn(carResponseDto);

        // When
        CarResponseDto result = carService.updateCar(1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(carRepository).findById(1L);
        verify(carMapper).updateEntityFromDto(updateDto, testCar);
        verify(carRepository).saveAndFlush(testCar);
        verify(carMapper).toResponse(testCar);
    }

    @Test
    void updateCar_ShouldThrowException_WhenCarNotFound() {
        // Given
        CarUpdateDto updateDto = new CarUpdateDto();
        updateDto.setId(1L);
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carService.updateCar(1L, updateDto))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Car not found: 1");
    }

    @Test
    void deleteCar_ShouldDeleteCar_WhenNoActiveBookings() {
        // Given
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(carAvailabilityRepository.hasActiveOrUpcomingForCar(
                eq(1L), any(Instant.class), eq(BookingStatus.activeSet()))).thenReturn(false);

        // When
        carService.deleteCar(1L);

        // Then
        verify(carRepository).findById(1L);
        verify(carAvailabilityRepository).hasActiveOrUpcomingForCar(
                eq(1L), any(Instant.class), eq(BookingStatus.activeSet()));
        verify(carRepository).delete(testCar);
    }

    @Test
    void deleteCar_ShouldThrowException_WhenActiveBookingsExist() {
        // Given
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(carAvailabilityRepository.hasActiveOrUpcomingForCar(
                eq(1L), any(Instant.class), eq(BookingStatus.activeSet()))).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> carService.deleteCar(1L))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessage("Car has active or upcoming bookings");

        verify(carRepository, never()).delete(any());
    }

    @Test
    void deleteCar_ShouldThrowException_WhenCarNotFound() {
        // Given
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carService.deleteCar(1L))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Car not found: 1");
    }

    @Test
    void findByCompanyId_ShouldReturnPagedResults() {
        // Given
        Page<Car> carPage = new PageImpl<>(Arrays.asList(testCar));

        when(carRepository.findByCompanyId(eq(1L), any(Pageable.class))).thenReturn(carPage);
        when(carMapper.toResponse(testCar)).thenReturn(carResponseDto);

        // When
        PageResponse<CarResponseDto> result = carService.findByCompanyId(1L, Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);

        verify(carRepository).findByCompanyId(eq(1L), any(Pageable.class));
        verify(carMapper).toResponse(testCar);
    }

    @Test
    void lockByIdForUpdate_ShouldReturnCar_WhenCarExists() {
        // Given
        when(carRepository.lockByIdForUpdate(1L)).thenReturn(Optional.of(testCar));

        // When
        Car result = carService.lockByIdForUpdate(1L);

        // Then
        assertThat(result).isEqualTo(testCar);
        verify(carRepository).lockByIdForUpdate(1L);
    }

    @Test
    void lockByIdForUpdate_ShouldThrowException_WhenCarNotFound() {
        // Given
        when(carRepository.lockByIdForUpdate(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> carService.lockByIdForUpdate(1L))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Car not found for locking: 1");
    }

    @Test
    void lockAndReserveIfAvailableOrThrow_ShouldSucceed_WhenCarAvailable() {
        // Given
        Instant from = Instant.now();
        Instant to = from.plusSeconds(3600);

        when(carRepository.lockByIdForUpdate(1L)).thenReturn(Optional.of(testCar));
        when(carAvailabilityRepository.existsActiveForCarInPeriod(
                eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()))).thenReturn(false);

        // When
        Car result = carService.lockAndReserveIfAvailableOrThrow(1L, from, to);

        // Then
        assertThat(result).isEqualTo(testCar);
    }

    @Test
    void lockAndReserveIfAvailableOrThrow_ShouldThrowException_WhenCarHasActiveBookings() {
        // Given
        Instant from = Instant.now();
        Instant to = from.plusSeconds(3600);

        when(carRepository.lockByIdForUpdate(1L)).thenReturn(Optional.of(testCar));
        when(carAvailabilityRepository.existsActiveForCarInPeriod(
                eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()))).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> carService.lockAndReserveIfAvailableOrThrow(1L, from, to))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessage("Car is not available");
    }
}
