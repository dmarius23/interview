package com.interview.booking.web;

import com.interview.booking.dto.BookingCreateByCarDto;
import com.interview.booking.dto.BookingCreateByModelDto;
import com.interview.booking.dto.BookingResponseDto;
import com.interview.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;

/**
 * REST controller for booking operations.
 */
@RestController
@RequestMapping("/api/bookings")
@Validated
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    /**
     * Create booking by specific car ID.
     */
    @PostMapping("/by-car")
    public BookingResponseDto createByCar(@Valid @RequestBody BookingCreateByCarDto req) {
        return bookingService.createBooking(req);
    }

    /**
     * Create booking by car model (system will select available car).
     */
    @PostMapping("/by-model")
    public BookingResponseDto createByModel(@Valid @RequestBody BookingCreateByModelDto req) {
        return bookingService.createBookingByModel(req);
    }

    /**
     * Cancel an existing booking.
     */
    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        bookingService.cancelBooking(id);
    }

    /**
     * Get all active bookings for a specific company.
     */
    @GetMapping("/company/{companyId}/active")
    public List<BookingResponseDto> getActiveBookingsForCompany(@PathVariable Long companyId) {
        return bookingService.getAllActiveBookingsForCompany(companyId);
    }

    /**
     * Get all active bookings for a specific location.
     */
    @GetMapping("/location/{locationId}/active")
    public List<BookingResponseDto> getActiveBookingsForLocation(@PathVariable Long locationId) {
        return bookingService.getActiveBookingsForLocation(locationId);
    }

    /**
     * Get bookings for a location within a specific time period.
     */
    @GetMapping("/location/{locationId}/period")
    public List<BookingResponseDto> getBookingsForLocationInPeriod(
            @PathVariable Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        return bookingService.getBookingsForLocationInPeriod(locationId, startTime, endTime);
    }
}