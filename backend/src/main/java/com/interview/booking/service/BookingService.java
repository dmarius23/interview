package com.interview.booking.service;

import com.interview.booking.dto.BookingCreateByCarDto;
import com.interview.booking.dto.BookingCreateByModelDto;
import com.interview.booking.dto.BookingResponseDto;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for booking operations.
 */
public interface BookingService {

    /**
     * Create a booking by car ID using DTO input and return DTO response.
     */
    BookingResponseDto createBooking(BookingCreateByCarDto dto);

    /**
     * Create a booking by model using DTO input and return DTO response.
     */
    BookingResponseDto createBookingByModel(BookingCreateByModelDto dto);

    /**
     * Create a booking by model and location/time window. Shortlist candidates and claim one using a pessimistic lock.
     */
    Long createBookingByModel(Long clientId,
                              Long carModelId,
                              Long pickupLocationId,
                              Long returnLocationId,
                              Instant pickup,
                              Instant ret,
                              String coupon);

    /**
     * Create a booking for a specific car ID using a pessimistic lock to avoid double-claiming.
     */
    Long createBooking(Long clientId,
                       Long carId,
                       Long pickupLocationId,
                       Long returnLocationId,
                       Instant pickup,
                       Instant ret,
                       String coupon);

    /**
     * Cancel an existing booking and release the car.
     */
    void cancelBooking(Long bookingId);

    /**
     * Send booking confirmation asynchronously.
     */
    void sendConfirmationAsync(Long bookingId);

    /**
     * Get all active bookings for a company (started and future bookings).
     */
    List<BookingResponseDto> getAllActiveBookingsForCompany(Long companyId);

    /**
     * Get all active bookings for a specific location (active and future).
     */
    List<BookingResponseDto> getActiveBookingsForLocation(Long locationId);

    /**
     * Get all bookings for a location that overlap with the specified time period.
     */
    List<BookingResponseDto> getBookingsForLocationInPeriod(Long locationId, Instant startTime, Instant endTime);
}