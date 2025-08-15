package com.interview.company;

import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.repo.BookingRepository;
import com.interview.common.TestDataFactory;
import com.interview.common.domain.BusinessRuleViolation;
import com.interview.common.domain.EntityNotFound;
import com.interview.common.web.PageResponse;
import com.interview.company.domain.RentalCompany;
import com.interview.company.domain.RentalLocation;
import com.interview.company.dto.RentalLocationCreateDto;
import com.interview.company.dto.RentalLocationResponseDto;
import com.interview.company.dto.RentalLocationUpdateDto;
import com.interview.company.mapper.RentalLocationMapper;
import com.interview.company.repo.RentalCompanyRepository;
import com.interview.company.repo.RentalLocationRepository;
import com.interview.company.service.impl.RentalLocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RentalLocationServiceTest {

    @Mock
    private RentalCompanyRepository companyRepository;

    @Mock
    private RentalLocationRepository rentalLocationRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RentalLocationMapper rentalLocationMapper;

    @InjectMocks
    private RentalLocationServiceImpl locationService;

    private RentalCompany testCompany;
    private RentalLocation testLocation;
    private RentalLocationCreateDto createDto;
    private RentalLocationUpdateDto updateDto;
    private RentalLocationResponseDto responseDto;

    @BeforeEach
    void setUp() {
        testCompany = TestDataFactory.createTestCompany();
        testCompany.setId(1L);

        testLocation = TestDataFactory.createTestLocation(testCompany);
        testLocation.setId(1L);
        testLocation.setVersion(0L);

        createDto = new RentalLocationCreateDto();
        createDto.setCompanyId(1L);
        createDto.setName("New Location");
        createDto.setCity("New City");
        createDto.setCountry("New Country");

        updateDto = new RentalLocationUpdateDto();
        updateDto.setName("Updated Location");
        updateDto.setCity("Updated City");
        updateDto.setCountry("Updated Country");
        updateDto.setVersion(0L);

        responseDto = new RentalLocationResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Test Location");
        responseDto.setCity("Test City");
        responseDto.setCountry("Test Country");
    }

    @Test
    void addRentalLocation_ShouldSucceed_WhenValidData() {

        when(rentalLocationMapper.toEntity(createDto)).thenReturn(testLocation);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(rentalLocationRepository.save(testLocation)).thenReturn(testLocation);
        when(rentalLocationMapper.toResponse(testLocation)).thenReturn(responseDto);

        RentalLocationResponseDto result = locationService.addRentalLocation(createDto);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(rentalLocationMapper).toEntity(createDto);
        verify(companyRepository).findById(1L);
        verify(rentalLocationRepository).save(testLocation);
        verify(rentalLocationMapper).toResponse(testLocation);
        assertThat(testLocation.getCompany()).isEqualTo(testCompany);
    }

    @Test
    void addRentalLocation_ShouldThrowException_WhenCompanyNotFound() {

        when(rentalLocationMapper.toEntity(createDto)).thenReturn(testLocation);
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> locationService.addRentalLocation(createDto))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Company not found: 1");
    }

    @Test
    void updateRentalLocation_ShouldSucceed_WhenLocationExists() {

        when(rentalLocationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(rentalLocationRepository.saveAndFlush(testLocation)).thenReturn(testLocation);
        when(rentalLocationMapper.toResponse(testLocation)).thenReturn(responseDto);


        RentalLocationResponseDto result = locationService.updateRentalLocation(1L, updateDto);


        assertThat(result).isNotNull();
        verify(rentalLocationRepository).findById(1L);
        verify(rentalLocationMapper).updateEntityFromDto(updateDto, testLocation);
        verify(rentalLocationRepository).saveAndFlush(testLocation);
        verify(rentalLocationMapper).toResponse(testLocation);
    }

    @Test
    void updateRentalLocation_ShouldThrowException_WhenLocationNotFound() {

        when(rentalLocationRepository.findById(1L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> locationService.updateRentalLocation(1L, updateDto))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Location not found: 1");
    }

    @Test
    void updateRentalLocation_ShouldRetryOnOptimisticLocking() {

        when(rentalLocationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(rentalLocationRepository.saveAndFlush(testLocation))
                .thenThrow(new OptimisticLockingFailureException("Optimistic lock"))
                .thenReturn(testLocation); // Success on second attempt
        when(rentalLocationMapper.toResponse(testLocation)).thenReturn(responseDto);


        RentalLocationResponseDto result = locationService.updateRentalLocation(1L, updateDto);


        assertThat(result).isNotNull();
        verify(rentalLocationRepository, times(2)).findById(1L);
        verify(rentalLocationRepository, times(2)).saveAndFlush(testLocation);
    }

    @Test
    void deleteRentalLocationSoft_ShouldSucceed_WhenNoOngoingBookings() {

        when(rentalLocationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(bookingRepository.hasOngoingForLocation(eq(1L), any(Instant.class), eq(BookingStatus.activeSet())))
                .thenReturn(false);
        when(bookingRepository.findFutureActiveForLocation(eq(1L), any(Instant.class), eq(BookingStatus.activeSet())))
                .thenReturn(Collections.emptyList());


        locationService.deleteRentalLocationSoft(1L);


        verify(rentalLocationRepository).findById(1L);
        verify(bookingRepository).hasOngoingForLocation(eq(1L), any(Instant.class), eq(BookingStatus.activeSet()));
        verify(rentalLocationRepository).delete(testLocation);
        assertThat(testLocation.isDeleted()).isTrue();
        assertThat(testLocation.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteRentalLocationSoft_ShouldThrowException_WhenOngoingBookingsExist() {

        when(rentalLocationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(bookingRepository.hasOngoingForLocation(eq(1L), any(Instant.class), eq(BookingStatus.activeSet())))
                .thenReturn(true);

        assertThatThrownBy(() -> locationService.deleteRentalLocationSoft(1L))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessage("Cannot delete location: ongoing bookings exist.");

        verify(rentalLocationRepository, never()).delete(any());
    }

    @Test
    void deleteRentalLocationSoft_ShouldCancelFutureBookings() {
        Booking futureBooking = new Booking();
        futureBooking.setId(1L);
        futureBooking.setStatus(BookingStatus.CONFIRMED);

        when(rentalLocationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(bookingRepository.hasOngoingForLocation(eq(1L), any(Instant.class), eq(BookingStatus.activeSet())))
                .thenReturn(false);
        when(bookingRepository.findFutureActiveForLocation(eq(1L), any(Instant.class), eq(BookingStatus.activeSet())))
                .thenReturn(Arrays.asList(futureBooking));

        locationService.deleteRentalLocationSoft(1L);

        assertThat(futureBooking.getStatus()).isEqualTo(BookingStatus.CANCELED);
        verify(rentalLocationRepository).delete(testLocation);
    }

    @Test
    void listLocationsByCity_ShouldReturnPagedResults() {
        Page<RentalLocation> page = new PageImpl<>(Arrays.asList(testLocation));
        when(rentalLocationRepository.findByCityIgnoreCase("Test City", Pageable.unpaged())).thenReturn(page);
        when(rentalLocationMapper.toResponse(testLocation)).thenReturn(responseDto);

        PageResponse<RentalLocationResponseDto> result = locationService.listLocationsByCity("Test City", Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(rentalLocationRepository).findByCityIgnoreCase("Test City", Pageable.unpaged());
    }

    @Test
    void listLocationsByCompanyId_ShouldReturnPagedResults() {
        Page<RentalLocation> page = new PageImpl<>(Arrays.asList(testLocation));
        when(rentalLocationRepository.findByCompanyId(1L, Pageable.unpaged())).thenReturn(page);
        when(rentalLocationMapper.toResponse(testLocation)).thenReturn(responseDto);

        PageResponse<RentalLocationResponseDto> result = locationService.listLocationsByCompanyId(1L, Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(rentalLocationRepository).findByCompanyId(1L, Pageable.unpaged());
    }

    @Test
    void getById_ShouldReturnLocation_WhenExists() {
        when(rentalLocationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(rentalLocationMapper.toResponse(testLocation)).thenReturn(responseDto);

        RentalLocationResponseDto result = locationService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(rentalLocationRepository).findById(1L);
        verify(rentalLocationMapper).toResponse(testLocation);
    }

    @Test
    void getById_ShouldThrowException_WhenNotFound() {
        when(rentalLocationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.getById(1L))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Rental location not found: 1");
    }

    @Test
    void getEntityById_ShouldReturnEntity_WhenExists() {
        when(rentalLocationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        RentalLocation result = locationService.getEntityById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(rentalLocationRepository).findById(1L);
    }

    @Test
    void getEntityById_ShouldThrowException_WhenNotFound() {
        when(rentalLocationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.getEntityById(1L))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("RentalLocation not found: 1");
    }
}

