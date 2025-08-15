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

@RestController
@RequestMapping("/api/bookings")
@Validated
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping("/by-car")
    public BookingResponseDto createByCar(@Valid @RequestBody BookingCreateByCarDto req) {
        return bookingService.createBooking(req);
    }

    @PostMapping("/by-model")
    public BookingResponseDto createByModel(@Valid @RequestBody BookingCreateByModelDto req) {
        return bookingService.createBookingByModel(req);
    }

    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        bookingService.cancelBooking(id);
    }

    @GetMapping("/company/{companyId}/active")
    public List<BookingResponseDto> getActiveBookingsForCompany(@PathVariable Long companyId) {
        return bookingService.getAllActiveBookingsForCompany(companyId);
    }

    @GetMapping("/location/{locationId}/active")
    public List<BookingResponseDto> getActiveBookingsForLocation(@PathVariable Long locationId) {
        return bookingService.getActiveBookingsForLocation(locationId);
    }

    @GetMapping("/location/{locationId}/period")
    public List<BookingResponseDto> getBookingsForLocationInPeriod(
            @PathVariable Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        return bookingService.getBookingsForLocationInPeriod(locationId, startTime, endTime);
    }
}