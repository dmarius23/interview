package com.interview.booking.service.impl;

import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.dto.BookingCreateByCarDto;
import com.interview.booking.dto.BookingCreateByModelDto;
import com.interview.booking.dto.BookingResponseDto;
import com.interview.booking.mapper.BookingMapper;
import com.interview.booking.repo.BookingRepository;
import com.interview.booking.service.BookingService;
import com.interview.booking.service.DiscountService;
import com.interview.client.repo.ClientRepository;
import com.interview.common.annotation.Loggable;
import com.interview.common.domain.BusinessRuleViolation;
import com.interview.common.domain.EntityNotFound;
import com.interview.company.service.RentalLocationService;
import com.interview.fleet.domain.Car;
import com.interview.fleet.service.CarAvailabilityService;
import com.interview.fleet.service.CarService;
import com.interview.payment.service.impl.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Loggable(logParams = true, logResult = false)
@RequiredArgsConstructor
@Transactional(readOnly = true) // Default for read operations
public class BookingServiceImpl implements BookingService {

    private final CarService carService;
    private final CarAvailabilityService carAvailabilityService;
    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;
    private final RentalLocationService rentalLocationService;
    private final DiscountService discountService;
    private final PaymentServiceImpl paymentServiceImpl;
    private final BookingMapper bookingMapper;
    private final BookingPaymentOrchestrator bookingPaymentOrchestrator;

    /**
     * Create a booking by car ID using DTO input and return DTO response.
     */
    @Override
    @Transactional // Override class-level read-only for write operation
    public BookingResponseDto createBooking(BookingCreateByCarDto dto) {
        Long bookingId = createBookingInternal(
                dto.getClientId(),
                dto.getCarId(),
                dto.getPickupLocationId(),
                dto.getReturnLocationId(),
                dto.getPickup(),
                dto.getRet(),
                dto.getCoupon()
        );

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFound("Booking not found: " + bookingId));
        return bookingMapper.toResponse(booking);
    }

    /**
     * Create a booking by model using DTO input and return DTO response.
     */
    @Override
    @Transactional // Override class-level read-only for write operation
    public BookingResponseDto createBookingByModel(BookingCreateByModelDto dto) {
        Long bookingId = createBookingByModelInternal(
                dto.getClientId(),
                dto.getCarModelId(),
                dto.getPickupLocationId(),
                dto.getReturnLocationId(),
                dto.getPickup(),
                dto.getRet(),
                dto.getCoupon()
        );

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFound("Booking not found: " + bookingId));
        return bookingMapper.toResponse(booking);
    }

    /**
     * Create a booking by model and location/time window. Shortlist candidates and claim one using a pessimistic lock.
     */
    @Override
    @Transactional // Override class-level read-only for write operation
    public Long createBookingByModel(Long clientId,
                                     Long carModelId,
                                     Long pickupLocationId,
                                     Long returnLocationId,
                                     Instant pickup,
                                     Instant ret,
                                     String coupon) {
        return createBookingByModelInternal(clientId, carModelId, pickupLocationId, returnLocationId, pickup, ret, coupon);
    }

    /**
     * Create a booking for a specific car ID using a pessimistic lock to avoid double-claiming.
     */
    @Override
    @Transactional // Override class-level read-only for write operation
    public Long createBooking(Long clientId,
                              Long carId,
                              Long pickupLocationId,
                              Long returnLocationId,
                              Instant pickup,
                              Instant ret,
                              String coupon) {
        return createBookingInternal(clientId, carId, pickupLocationId, returnLocationId, pickup, ret, coupon);
    }

    /**
     * Cancel an existing booking and release the car.
     */
    @Override
    @Transactional // Override class-level read-only for write operation
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFound("Booking not found: " + bookingId));

        if (booking.getStatus().isTerminal()) {
            return; // idempotent - booking already in terminal state
        }

        booking.setStatus(BookingStatus.CANCELED);
        //booking.getCar().setStatus(CarStatus.AVAILABLE);
    }

    @Override
    @Async
    public void sendConfirmationAsync(Long bookingId) {
        // email / invoice / event
    }

    /**
     * Get all active bookings for a company (started and future bookings).
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllActiveBookingsForCompany(Long companyId) {
        Instant now = Instant.now();
        List<Booking> activeBookings = bookingRepository.findActiveBookingsForCompany(companyId, now);
        return activeBookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all active bookings for a specific location (active and future).
     */
    @Override
    public List<BookingResponseDto> getActiveBookingsForLocation(Long locationId) {
        Instant now = Instant.now();
        List<Booking> activeBookings = bookingRepository.findActiveBookingsForLocation(locationId, now);
        return activeBookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings for a location that overlap with the specified time period.
     */
    @Override
    public List<BookingResponseDto> getBookingsForLocationInPeriod(Long locationId, Instant startTime, Instant endTime) {
        List<Booking> overlappingBookings = bookingRepository.findBookingsForLocationInPeriod(locationId, startTime, endTime);
        return overlappingBookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }


    private Long createBookingByModelInternal(Long clientId,
                                              Long carModelId,
                                              Long pickupLocationId,
                                              Long returnLocationId,
                                              Instant pickup,
                                              Instant ret,
                                              String coupon) {
        List<Long> candidateIds = shortlistCandidateCarIds(carModelId, pickupLocationId, pickup, ret);
        Car claimed = claimFirstAvailable(candidateIds, pickup, ret)
                .orElseThrow(() -> new BusinessRuleViolation("Inventory changed; no cars available at this moment."));
        return finalizeBookingFlow(clientId, claimed, pickupLocationId, returnLocationId, pickup, ret, coupon);
    }

    private Long createBookingInternal(Long clientId,
                                       Long carId,
                                       Long pickupLocationId,
                                       Long returnLocationId,
                                       Instant pickup,
                                       Instant ret,
                                       String coupon) {
        Car claimed = claimCarByIdOrThrow(carId, pickup, ret);
        return finalizeBookingFlow(clientId, claimed, pickupLocationId, returnLocationId, pickup, ret, coupon);
    }

    private List<Long> shortlistCandidateCarIds(Long carModelId, Long locationId, Instant from, Instant to) {
        // limit to a small batch size to reduce lock contention
        return carAvailabilityService.findAvailableCarIds(carModelId, locationId, from, to, PageRequest.of(0, 10));
    }

    private Optional<Car> claimFirstAvailable(List<Long> candidateIds, Instant from, Instant to) {
        for (Long id : candidateIds) {
            try {
                Car c = carService.lockByIdForUpdate(id);
                if (carAvailabilityService.isCarAvailable(c.getId(), from, to)) {
                    return Optional.of(c);
                }
            } catch (PessimisticLockingFailureException e) {

            }
        }
        return Optional.empty();
    }


    private Car claimCarByIdOrThrow(Long carId, Instant from, Instant to) {
        Car c = carService.lockByIdForUpdate(carId);

        if (!carAvailabilityService.isCarAvailable(carId, from, to)) {
            throw new BusinessRuleViolation("Car is not available");
        }

        return c;
    }

    private Long finalizeBookingFlow(Long clientId,
                                     Car car,
                                     Long pickupLocationId,
                                     Long returnLocationId,
                                     Instant pickup,
                                     Instant ret,
                                     String coupon) {
        long days = ChronoUnit.DAYS.between(pickup, ret);
        if (days <= 0) {
            throw new BusinessRuleViolation("Return date must be after pickup date");
        }

        int base = (int) (car.getDailyPriceInCents() * days);
        int finalPrice;
        try {
            finalPrice = discountService.applyPromotions(clientId, base, coupon);
        } catch (RuntimeException ex) {
            // NESTED transaction failed; proceed without discount
            // This part is just to show the NESTED transaction.
            // In production the user needs to be notify that the coupon is not valid !

            finalPrice = base;
        }

        Booking booking = buildAndPersistBooking(clientId, car, pickupLocationId, returnLocationId, pickup, ret, finalPrice);
//        Payment payment = createAndPersistPayment(booking, finalPrice);
//        paymentServiceImpl.charge(payment);
        bookingPaymentOrchestrator.startBookingSaga(booking);
        return booking.getId();
    }

    private Booking buildAndPersistBooking(Long clientId,
                                           Car claimed,
                                           Long pickupLocationId,
                                           Long returnLocationId,
                                           Instant pickup,
                                           Instant ret,
                                           int totalPriceCents) {
        Booking booking = new Booking();

        booking.setClient(clientRepository.getOne(clientId));
        booking.setCar(claimed);
        booking.setPickupLocation(rentalLocationService.getEntityById(pickupLocationId));
        booking.setReturnLocation(rentalLocationService.getEntityById(returnLocationId));
        booking.setPickupTime(pickup);
        booking.setReturnTime(ret);
        booking.setStatus(BookingStatus.CREATED);
        booking.setTotalPriceCents(totalPriceCents);
        return bookingRepository.save(booking);
    }

//    private Payment createAndPersistPayment(Booking booking, int amountCents) {
//        Payment payment = new Payment();
//        payment.setBooking(booking);
//        payment.setAmountCents(amountCents);
//        return paymentServiceImpl.charge(payment);
//    }

}