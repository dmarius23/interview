package com.interview.booking;

import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.repo.BookingRepository;
import com.interview.catalog.domain.CarModel;
import com.interview.catalog.repo.CarModelRepository;
import com.interview.client.domain.Client;
import com.interview.client.repo.ClientRepository;
import com.interview.common.BaseRepositoryTest;
import com.interview.common.TestDataFactory;
import com.interview.company.domain.RentalCompany;
import com.interview.company.domain.RentalLocation;
import com.interview.company.repo.RentalCompanyRepository;
import com.interview.company.repo.RentalLocationRepository;
import com.interview.fleet.domain.Car;
import com.interview.fleet.repo.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private RentalCompanyRepository companyRepository;

    @Autowired
    private RentalLocationRepository locationRepository;

    private Client testClient;
    private Car testCar;
    private RentalLocation testLocation;
    private Booking testBooking;

    @BeforeEach
    void setUp() {

        RentalCompany company = TestDataFactory.createTestCompany();
        company = companyRepository.save(company);

        testLocation = TestDataFactory.createTestLocation(company);
        testLocation = locationRepository.save(testLocation);

        CarModel carModel = TestDataFactory.createTestCarModel();
        carModel = carModelRepository.save(carModel);

        testCar = TestDataFactory.createTestCar(company, carModel, testLocation);
        testCar = carRepository.save(testCar);

        testClient = TestDataFactory.createTestClient();
        testClient = clientRepository.save(testClient);

        testBooking = TestDataFactory.createTestBooking(testClient, testCar, testLocation, testLocation);
        testBooking = bookingRepository.save(testBooking);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findActiveBookingsForCompany_ShouldReturnActiveBookings() {

        Instant now = Instant.now();


        List<Booking> activeBookings = bookingRepository.findActiveBookingsForCompany(
                testCar.getCompany().getId(), now);


        assertThat(activeBookings).hasSize(1);
        assertThat(activeBookings.get(0).getId()).isEqualTo(testBooking.getId());
    }

    @Test
    void findActiveBookingsForLocation_ShouldReturnActiveBookings() {

        Instant now = Instant.now();


        List<Booking> activeBookings = bookingRepository.findActiveBookingsForLocation(
                testLocation.getId(), now);


        assertThat(activeBookings).hasSize(1);
        assertThat(activeBookings.get(0).getId()).isEqualTo(testBooking.getId());
    }

    @Test
    void hasOngoingForCompany_ShouldReturnTrue_WhenOngoingBookingExists() {

        testBooking.setPickupTime(Instant.now().minus(1, ChronoUnit.HOURS));
        testBooking.setReturnTime(Instant.now().plus(1, ChronoUnit.HOURS));
        testBooking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(testBooking);
        entityManager.flush();


        boolean hasOngoing = bookingRepository.hasOngoingForCompany(
                testCar.getCompany().getId(),
                Instant.now(),
                BookingStatus.activeSet()
        );


        assertThat(hasOngoing).isTrue();
    }

    @Test
    void hasOngoingForCompany_ShouldReturnFalse_WhenNoOngoingBookingExists() {

        testBooking.setPickupTime(Instant.now().plus(1, ChronoUnit.DAYS));
        testBooking.setReturnTime(Instant.now().plus(2, ChronoUnit.DAYS));
        bookingRepository.save(testBooking);
        entityManager.flush();


        boolean hasOngoing = bookingRepository.hasOngoingForCompany(
                testCar.getCompany().getId(),
                Instant.now(),
                BookingStatus.activeSet()
        );


        assertThat(hasOngoing).isFalse();
    }

    @Test
    void findBookingsForLocationInPeriod_ShouldReturnOverlappingBookings() {

        Instant periodStart = testBooking.getPickupTime().minus(1, ChronoUnit.HOURS);
        Instant periodEnd = testBooking.getReturnTime().plus(1, ChronoUnit.HOURS);


        List<Booking> overlappingBookings = bookingRepository.findBookingsForLocationInPeriod(
                testLocation.getId(), periodStart, periodEnd);


        assertThat(overlappingBookings).hasSize(1);
        assertThat(overlappingBookings.get(0).getId()).isEqualTo(testBooking.getId());
    }
}

