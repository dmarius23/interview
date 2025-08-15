package com.interview.company;

import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.repo.BookingRepository;
import com.interview.common.TestDataFactory;
import com.interview.common.domain.BusinessRuleViolation;
import com.interview.common.domain.EntityNotFound;
import com.interview.common.web.PageResponse;
import com.interview.company.domain.RentalCompany;
import com.interview.company.dto.RentalCompanyCreateDto;
import com.interview.company.dto.RentalCompanyResponseDto;
import com.interview.company.dto.RentalCompanyUpdateDto;
import com.interview.company.mapper.RentalCompanyMapper;
import com.interview.company.repo.RentalCompanyRepository;
import com.interview.company.service.impl.RentalCompanyServiceImpl;
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
class RentalCompanyServiceTest {

    @Mock
    private RentalCompanyRepository companyRepo;

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private RentalCompanyMapper rentalCompanyMapper;

    @InjectMocks
    private RentalCompanyServiceImpl companyService;

    private RentalCompany testCompany;
    private RentalCompanyCreateDto createDto;
    private RentalCompanyUpdateDto updateDto;
    private RentalCompanyResponseDto responseDto;

    @BeforeEach
    void setUp() {
        testCompany = TestDataFactory.createTestCompany();
        testCompany.setId(1L);
        testCompany.setVersion(0L);

        createDto = new RentalCompanyCreateDto();
        createDto.setName("New Rental Company");

        updateDto = new RentalCompanyUpdateDto();
        updateDto.setName("Updated Company Name");
        updateDto.setVersion(0L);

        responseDto = new RentalCompanyResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Test Rental Company");
    }

    @Test
    void addCompany_ShouldSucceed_WhenValidData() {
        // Given
        when(rentalCompanyMapper.toEntity(createDto)).thenReturn(testCompany);
        when(companyRepo.save(testCompany)).thenReturn(testCompany);
        when(rentalCompanyMapper.toResponse(testCompany)).thenReturn(responseDto);


        RentalCompanyResponseDto result = companyService.addCompany(createDto);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(rentalCompanyMapper).toEntity(createDto);
        verify(companyRepo).save(testCompany);
        verify(rentalCompanyMapper).toResponse(testCompany);
    }

    @Test
    void updateCompany_ShouldSucceed_WhenCompanyExists() {

        when(companyRepo.findById(1L)).thenReturn(Optional.of(testCompany));
        when(companyRepo.saveAndFlush(testCompany)).thenReturn(testCompany);
        when(rentalCompanyMapper.toResponse(testCompany)).thenReturn(responseDto);


        RentalCompanyResponseDto result = companyService.updateCompany(1L, updateDto);


        assertThat(result).isNotNull();
        verify(companyRepo).findById(1L);
        verify(rentalCompanyMapper).updateEntityFromDto(updateDto, testCompany);
        verify(companyRepo).saveAndFlush(testCompany);
        verify(rentalCompanyMapper).toResponse(testCompany);
    }

    @Test
    void updateCompany_ShouldThrowException_WhenCompanyNotFound() {

        when(companyRepo.findById(1L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> companyService.updateCompany(1L, updateDto))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Company not found: 1");
    }

    @Test
    void updateCompany_ShouldRetryOnOptimisticLocking() {

        when(companyRepo.findById(1L)).thenReturn(Optional.of(testCompany));
        when(companyRepo.saveAndFlush(testCompany))
                .thenThrow(new OptimisticLockingFailureException("Optimistic lock"))
                .thenReturn(testCompany); // Success on second attempt
        when(rentalCompanyMapper.toResponse(testCompany)).thenReturn(responseDto);


        RentalCompanyResponseDto result = companyService.updateCompany(1L, updateDto);


        assertThat(result).isNotNull();
        verify(companyRepo, times(2)).findById(1L);
        verify(companyRepo, times(2)).saveAndFlush(testCompany);
    }

    @Test
    void deleteCompanySoft_ShouldSucceed_WhenNoOngoingBookings() {

        when(companyRepo.findById(1L)).thenReturn(Optional.of(testCompany));
        when(bookingRepo.hasOngoingForCompany(eq(1L), any(Instant.class), eq(BookingStatus.activeSet())))
                .thenReturn(false);
        when(bookingRepo.findFutureActiveForCompany(eq(1L), any(Instant.class), eq(BookingStatus.activeSet())))
                .thenReturn(Collections.emptyList());


        companyService.deleteCompanySoft(1L);


        verify(companyRepo).findById(1L);
        verify(bookingRepo).hasOngoingForCompany(eq(1L), any(Instant.class), eq(BookingStatus.activeSet()));
        verify(companyRepo).delete(testCompany);
        assertThat(testCompany.isDeleted()).isTrue();
        assertThat(testCompany.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteCompanySoft_ShouldThrowException_WhenOngoingBookingsExist() {

        when(companyRepo.findById(1L)).thenReturn(Optional.of(testCompany));
        when(bookingRepo.hasOngoingForCompany(eq(1L), any(Instant.class), eq(BookingStatus.activeSet())))
                .thenReturn(true);


        assertThatThrownBy(() -> companyService.deleteCompanySoft(1L))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessage("Cannot delete company: ongoing bookings exist.");

        verify(companyRepo, never()).delete(any());
    }

    @Test
    void deleteCompanySoft_ShouldCancelFutureBookings() {

        Booking futureBooking = new Booking();
        futureBooking.setId(1L);
        futureBooking.setStatus(BookingStatus.CONFIRMED);

        when(companyRepo.findById(1L)).thenReturn(Optional.of(testCompany));
        when(bookingRepo.hasOngoingForCompany(eq(1L), any(Instant.class), eq(BookingStatus.activeSet())))
                .thenReturn(false);
        when(bookingRepo.findFutureActiveForCompany(eq(1L), any(Instant.class), eq(BookingStatus.activeSet())))
                .thenReturn(Arrays.asList(futureBooking));


        companyService.deleteCompanySoft(1L);


        assertThat(futureBooking.getStatus()).isEqualTo(BookingStatus.CANCELED);
        verify(companyRepo).delete(testCompany);
    }

    @Test
    void listCompanies_ShouldReturnPagedResults() {

        Page<RentalCompany> page = new PageImpl<>(Arrays.asList(testCompany));
        when(companyRepo.findAll(any(Pageable.class))).thenReturn(page);
        when(rentalCompanyMapper.toResponse(testCompany)).thenReturn(responseDto);


        PageResponse<RentalCompanyResponseDto> result = companyService.listCompanies(Pageable.unpaged());


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(companyRepo).findAll(any(Pageable.class));
    }

    @Test
    void getCompany_ShouldReturnCompany_WhenExists() {

        when(companyRepo.findById(1L)).thenReturn(Optional.of(testCompany));
        when(rentalCompanyMapper.toResponse(testCompany)).thenReturn(responseDto);


        RentalCompanyResponseDto result = companyService.getCompany(1L);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(companyRepo).findById(1L);
        verify(rentalCompanyMapper).toResponse(testCompany);
    }

    @Test
    void getCompany_ShouldThrowException_WhenNotFound() {

        when(companyRepo.findById(1L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> companyService.getCompany(1L))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Company not found: 1");
    }
}

