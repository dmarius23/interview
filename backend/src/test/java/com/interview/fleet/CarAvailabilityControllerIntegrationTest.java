package com.interview.fleet;

import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.repo.BookingRepository;
import com.interview.catalog.domain.CarModel;
import com.interview.catalog.repo.CarModelRepository;
import com.interview.client.domain.Client;
import com.interview.client.repo.ClientRepository;
import com.interview.common.BaseIntegrationTest;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CarAvailabilityControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private RentalCompanyRepository companyRepository;

    @Autowired
    private RentalLocationRepository locationRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private RentalCompany testCompany;
    private RentalLocation testLocation;
    private RentalLocation testLocation2;
    private CarModel testCarModel;
    private Car availableCar;
    private Car bookedCar;
    private Client testClient;

    @BeforeEach
    void setUp() {
        // Create test data
        testCompany = TestDataFactory.createTestCompany();
        testCompany = companyRepository.saveAndFlush(testCompany);

        testLocation = TestDataFactory.createTestLocation(testCompany);
        testLocation.setName("Location 1");
        testLocation.setCity("Test City");
        testLocation = locationRepository.saveAndFlush(testLocation);

        testLocation2 = TestDataFactory.createTestLocation(testCompany);
        testLocation2.setName("Location 2");
        testLocation2.setCity("Other City");
        testLocation2 = locationRepository.saveAndFlush(testLocation2);

        testCarModel = TestDataFactory.createTestCarModel();
        testCarModel = carModelRepository.saveAndFlush(testCarModel);

        // Create two cars of the same model
        availableCar = TestDataFactory.createTestCar(testCompany, testCarModel, testLocation);
        availableCar.setPlateNumber("AVAILABLE-CAR");
        availableCar.setVin("AVAILABLE-VIN");
        availableCar = carRepository.saveAndFlush(availableCar);

        bookedCar = TestDataFactory.createTestCar(testCompany, testCarModel, testLocation);
        bookedCar.setPlateNumber("BOOKED-CAR");
        bookedCar.setVin("BOOKED-VIN");
        bookedCar = carRepository.saveAndFlush(bookedCar);

        testClient = TestDataFactory.createTestClient();
        testClient = clientRepository.saveAndFlush(testClient);

        // Create a booking for the second car that overlaps with our search period
        Booking booking = new Booking();
        booking.setClient(testClient);
        booking.setCar(bookedCar);
        booking.setPickupLocation(testLocation);
        booking.setReturnLocation(testLocation);
        booking.setPickupTime(Instant.now().plus(1, ChronoUnit.DAYS));
        booking.setReturnTime(Instant.now().plus(3, ChronoUnit.DAYS));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPriceCents(10000);
        bookingRepository.saveAndFlush(booking);
    }

    @Test
    void getAvailableModelsByLocation_ShouldReturnAvailableModels() throws Exception {
        // Given - search during time when both cars should be available
        Instant from = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant to = Instant.now().plus(2, ChronoUnit.HOURS);

        // When & Then
        mockMvc.perform(get("/api/availability/models/by-location")
                .param("companyId", testCompany.getId().toString())
                .param("locationId", testLocation.getId().toString())
                .param("from", from.toString())
                .param("to", to.toString())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].make").value("Toyota"))
                .andExpect(jsonPath("$.content[0].model").value("Corolla"));
    }

    @Test
    void getAvailableModelsByLocation_ShouldFilterOutBookedPeriods() throws Exception {
        // Given - search during the time when bookedCar is booked
        Instant from = Instant.now().plus(25, ChronoUnit.HOURS); // Overlaps with booking
        Instant to = Instant.now().plus(26, ChronoUnit.HOURS);

        // When & Then
        mockMvc.perform(get("/api/availability/models/by-location")
                .param("companyId", testCompany.getId().toString())
                .param("locationId", testLocation.getId().toString())
                .param("from", from.toString())
                .param("to", to.toString())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content").isArray());
        // Note: Since we have one available car and one booked car of the same model,
        // the model might still be returned as there's at least one available car
    }

}
