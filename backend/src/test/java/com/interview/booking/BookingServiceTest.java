package com.interview.booking;

import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.dto.BookingCreateByCarDto;
import com.interview.booking.dto.BookingResponseDto;
import com.interview.booking.mapper.BookingMapper;
import com.interview.booking.repo.BookingRepository;
import com.interview.booking.service.DiscountService;
import com.interview.booking.service.impl.BookingPaymentOrchestrator;
import com.interview.booking.service.impl.BookingServiceImpl;
import com.interview.client.repo.ClientRepository;
import com.interview.common.TestDataFactory;
import com.interview.common.domain.BusinessRuleViolation;
import com.interview.common.domain.EntityNotFound;
import com.interview.company.service.RentalLocationService;
import com.interview.fleet.domain.Car;
import com.interview.fleet.service.CarAvailabilityService;
import com.interview.fleet.service.CarService;
import com.interview.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private CarService carService;
    @Mock
    private CarAvailabilityService carAvailabilityService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private RentalLocationService rentalLocationService;
    @Mock
    private DiscountService discountService;
    @Mock
    private PaymentServiceImpl paymentServiceImpl;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingPaymentOrchestrator bookingPaymentOrchestrator;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingCreateByCarDto bookingDto;
    private Car testCar;
    private Booking testBooking;

    @BeforeEach
    void setUp() {

        testCar = TestDataFactory.createTestCar(
                TestDataFactory.createTestCompany(),
                TestDataFactory.createTestCarModel(),
                TestDataFactory.createTestLocation(TestDataFactory.createTestCompany())
        );
        testCar.setId(1L);

        testBooking = TestDataFactory.createTestBooking(
                TestDataFactory.createTestClient(),
                testCar,
                testCar.getCurrentLocation(),
                testCar.getCurrentLocation()
        );
        testBooking.setId(1L);

        bookingDto = TestDataFactory.createBookingCreateByCarDto(1L, 1L, 1L);
    }

    @Test
    void createBooking_ShouldSucceed_WhenCarIsAvailable() {

        when(carService.lockByIdForUpdate(1L)).thenReturn(testCar);
        when(carAvailabilityService.isCarAvailable(eq(1L), any(), any())).thenReturn(true);
        when(clientRepository.getOne(1L)).thenReturn(testBooking.getClient());
        when(rentalLocationService.getEntityById(1L)).thenReturn(testCar.getCurrentLocation());
        when(discountService.applyPromotions(eq(1L), anyInt(), any())).thenReturn(10000);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        BookingResponseDto expectedResponse = new BookingResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setStatus("CREATED");
        when(bookingMapper.toResponse(testBooking)).thenReturn(expectedResponse);


        BookingResponseDto result = bookingService.createBooking(bookingDto);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("CREATED");

        verify(carService).lockByIdForUpdate(1L);
        verify(carAvailabilityService).isCarAvailable(eq(1L), any(), any());
        verify(bookingPaymentOrchestrator).startBookingSaga(any(Booking.class));
    }

    @Test
    void createBooking_ShouldThrowException_WhenCarNotAvailable() {

        when(carService.lockByIdForUpdate(1L)).thenReturn(testCar);
        when(carAvailabilityService.isCarAvailable(eq(1L), any(), any())).thenReturn(false);


        assertThatThrownBy(() -> bookingService.createBooking(bookingDto))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessage("Car is not available");

        verify(carService).lockByIdForUpdate(1L);
        verify(carAvailabilityService).isCarAvailable(eq(1L), any(), any());
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void createBooking_ShouldThrowException_WhenCarNotFound() {

        when(carService.lockByIdForUpdate(1L)).thenThrow(new EntityNotFound("Car not found: 1"));


        assertThatThrownBy(() -> bookingService.createBooking(bookingDto))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Car not found: 1");

        verify(carService).lockByIdForUpdate(1L);
        verifyNoInteractions(carAvailabilityService);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void createBooking_ShouldThrowException_WhenReturnDateBeforePickup() {

        bookingDto.setRet(bookingDto.getPickup().minus(1, ChronoUnit.DAYS));
        when(carService.lockByIdForUpdate(1L)).thenReturn(testCar);
        when(carAvailabilityService.isCarAvailable(eq(1L), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessage("Return date must be after pickup date");

        verify(carService).lockByIdForUpdate(1L);
        verify(carAvailabilityService).isCarAvailable(eq(1L), any(), any());
    }

    @Test
    void createBooking_ShouldHandleDiscountServiceFailure_Gracefully() {

        when(carService.lockByIdForUpdate(1L)).thenReturn(testCar);
        when(carAvailabilityService.isCarAvailable(eq(1L), any(), any())).thenReturn(true);
        when(clientRepository.getOne(1L)).thenReturn(testBooking.getClient());
        when(rentalLocationService.getEntityById(1L)).thenReturn(testCar.getCurrentLocation());
        when(discountService.applyPromotions(eq(1L), anyInt(), any()))
                .thenThrow(new RuntimeException("Discount service error"));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        BookingResponseDto expectedResponse = new BookingResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setStatus("CREATED");
        when(bookingMapper.toResponse(testBooking)).thenReturn(expectedResponse);


        BookingResponseDto result = bookingService.createBooking(bookingDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(bookingPaymentOrchestrator).startBookingSaga(any(Booking.class));
    }

    @Test
    void cancelBooking_ShouldSucceed_WhenBookingExists() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        bookingService.cancelBooking(1L);

        assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CANCELED);
    }

    @Test
    void cancelBooking_ShouldThrowException_WhenBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(1L))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Booking not found: 1");
    }

    @Test
    void cancelBooking_ShouldBeIdempotent_WhenBookingAlreadyCanceled() {
        testBooking.setStatus(BookingStatus.CANCELED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        bookingService.cancelBooking(1L);

        assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CANCELED);
    }

    @Test
    void cancelBooking_ShouldBeIdempotent_WhenBookingAlreadyCompleted() {
        testBooking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        bookingService.cancelBooking(1L);

        assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }
}