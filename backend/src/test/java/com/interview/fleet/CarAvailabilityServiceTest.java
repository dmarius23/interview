package com.interview.fleet;
import com.interview.booking.domain.BookingStatus;
import com.interview.catalog.domain.CarModel;
import com.interview.catalog.dto.CarModelResponseDto;
import com.interview.catalog.mapper.CarModelMapper;
import com.interview.catalog.repo.CarModelRepository;
import com.interview.common.TestDataFactory;
import com.interview.common.web.PageResponse;
import com.interview.fleet.domain.Car;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.mapper.CarMapper;
import com.interview.fleet.repo.CarAvailabilityRepository;
import com.interview.fleet.repo.dto.CarInfoDto;
import com.interview.fleet.service.impl.CarAvailabilityServiceImpl;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarAvailabilityServiceTest {

    @Mock
    private CarAvailabilityRepository carAvailabilityRepository;

    @Mock
    private CarModelRepository carModelRepository;

    @Mock
    private CarModelMapper carModelMapper;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarAvailabilityServiceImpl carAvailabilityService;

    private CarModel testCarModel;
    private Car testCar;
    private CarModelResponseDto carModelResponseDto;
    private CarResponseDto carResponseDto;
    private CarInfoDto carInfoDto;
    private Instant from;
    private Instant to;

    @BeforeEach
    void setUp() {
        testCarModel = TestDataFactory.createTestCarModel();
        testCarModel.setId(1L);

        testCar = TestDataFactory.createTestCar(
                TestDataFactory.createTestCompany(),
                testCarModel,
                TestDataFactory.createTestLocation(TestDataFactory.createTestCompany())
        );
        testCar.setId(1L);

        carModelResponseDto = new CarModelResponseDto();
        carModelResponseDto.setId(1L);
        carModelResponseDto.setMake("Toyota");
        carModelResponseDto.setModel("Corolla");

        carResponseDto = new CarResponseDto();
        carResponseDto.setId(1L);
        carResponseDto.setPlateNumber("TEST-123");

        carInfoDto = new CarInfoDto(1L, "TEST-123", testCar.getStatus(),
                "Toyota", "Corolla", "Test Company", "Test Location");

        from = Instant.now();
        to = from.plusSeconds(3600);
    }

    @Test
    void searchAvailable_ShouldReturnAvailableCars() {
        // Given
        Page<CarInfoDto> page = new PageImpl<>(Arrays.asList(carInfoDto));
        when(carAvailabilityRepository.findAvailableCars(
                eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<CarInfoDto> result = carAvailabilityService.searchAvailable(1L, from, to, Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(carInfoDto);
        verify(carAvailabilityRepository).findAvailableCars(
                eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class));
    }

    @Test
    void availableModelsAtLocationForCompany_ShouldReturnAvailableModels() {
        // Given
        Page<Long> modelIds = new PageImpl<>(Arrays.asList(1L));
        when(carAvailabilityRepository.findAvailableModelIdsAtLocationForCompany(
                eq(1L), eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class)))
                .thenReturn(modelIds);
        when(carModelRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(testCarModel));
        when(carModelMapper.toResponse(testCarModel)).thenReturn(carModelResponseDto);

        // When
        PageResponse<CarModelResponseDto> result = carAvailabilityService
                .availableModelsAtLocationForCompany(1L, 1L, from, to, Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        verify(carAvailabilityRepository).findAvailableModelIdsAtLocationForCompany(
                eq(1L), eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class));
    }

    @Test
    void availableModelsInCityForCompany_ShouldReturnAvailableModels() {
        // Given
        Page<Long> modelIds = new PageImpl<>(Arrays.asList(1L));
        when(carAvailabilityRepository.findAvailableModelIdsInCityForCompany(
                eq(1L), eq("Test City"), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class)))
                .thenReturn(modelIds);
        when(carModelRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(testCarModel));
        when(carModelMapper.toResponse(testCarModel)).thenReturn(carModelResponseDto);

        // When
        PageResponse<CarModelResponseDto> result = carAvailabilityService
                .availableModelsInCityForCompany(1L, "Test City", from, to, Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        verify(carAvailabilityRepository).findAvailableModelIdsInCityForCompany(
                eq(1L), eq("Test City"), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class));
    }

    @Test
    void getAvailableCarsByModelAtLocation_ShouldReturnAvailableCars() {
        // Given
        Page<Car> carPage = new PageImpl<>(Arrays.asList(testCar));
        when(carAvailabilityRepository.findAvailableCarsByModelAtLocation(
                eq(1L), eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class)))
                .thenReturn(carPage);
        when(carMapper.toResponse(testCar)).thenReturn(carResponseDto);

        // When
        PageResponse<CarResponseDto> result = carAvailabilityService
                .getAvailableCarsByModelAtLocation(1L, 1L, from, to, Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        verify(carAvailabilityRepository).findAvailableCarsByModelAtLocation(
                eq(1L), eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class));
    }

    @Test
    void findAvailableCarsByCity_ShouldReturnAvailableCars() {
        // Given
        Page<Car> carPage = new PageImpl<>(Arrays.asList(testCar));
        when(carAvailabilityRepository.findAvailableCarsByCity(
                eq("Test City"), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class)))
                .thenReturn(carPage);
        when(carMapper.toResponse(testCar)).thenReturn(carResponseDto);

        // When
        PageResponse<CarResponseDto> result = carAvailabilityService
                .findAvailableCarsByCity("Test City", from, to, Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        verify(carAvailabilityRepository).findAvailableCarsByCity(
                eq("Test City"), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class));
    }

    @Test
    void findAvailableCarIds_ShouldReturnCarIds() {
        // Given
        List<Long> carIds = Arrays.asList(1L, 2L, 3L);
        when(carAvailabilityRepository.findAvailableCarIds(
                eq(1L), eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class)))
                .thenReturn(carIds);

        // When
        List<Long> result = carAvailabilityService.findAvailableCarIds(1L, 1L, from, to, Pageable.unpaged());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(1L, 2L, 3L);
        verify(carAvailabilityRepository).findAvailableCarIds(
                eq(1L), eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()), any(Pageable.class));
    }

    @Test
    void isCarAvailable_ShouldReturnTrue_WhenNoActiveBookings() {
        // Given
        when(carAvailabilityRepository.existsActiveForCarInPeriod(
                eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet())))
                .thenReturn(false);

        // When
        boolean result = carAvailabilityService.isCarAvailable(1L, from, to);

        // Then
        assertThat(result).isTrue();
        verify(carAvailabilityRepository).existsActiveForCarInPeriod(
                eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()));
    }

    @Test
    void isCarAvailable_ShouldReturnFalse_WhenActiveBookingsExist() {
        // Given
        when(carAvailabilityRepository.existsActiveForCarInPeriod(
                eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet())))
                .thenReturn(true);

        // When
        boolean result = carAvailabilityService.isCarAvailable(1L, from, to);

        // Then
        assertThat(result).isFalse();
        verify(carAvailabilityRepository).existsActiveForCarInPeriod(
                eq(1L), eq(from), eq(to), eq(BookingStatus.activeSet()));
    }
}
