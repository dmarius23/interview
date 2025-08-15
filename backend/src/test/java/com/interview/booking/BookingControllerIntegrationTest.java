package com.interview.booking;

import com.interview.booking.dto.BookingCreateByCarDto;
import com.interview.booking.dto.BookingResponseDto;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingControllerIntegrationTest extends BaseIntegrationTest {

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

    @BeforeEach
    void setUp() {
        // Create test data
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
    }

    @Test
    void createBookingByCar_ShouldSucceed_WhenValidRequest() throws Exception {

        BookingCreateByCarDto request = new BookingCreateByCarDto();
        request.setClientId(testClient.getId());
        request.setCarId(testCar.getId());
        request.setPickupLocationId(testLocation.getId());
        request.setReturnLocationId(testLocation.getId());
        request.setPickup(Instant.now().plus(1, ChronoUnit.DAYS));
        request.setRet(Instant.now().plus(3, ChronoUnit.DAYS));


        MvcResult result = mockMvc.perform(post("/api/bookings/by-car")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.clientId").value(testClient.getId()))
                .andExpect(jsonPath("$.carId").value(testCar.getId()))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        BookingResponseDto response = objectMapper.readValue(responseContent, BookingResponseDto.class);
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void createBookingByCar_ShouldFail_WhenInvalidDateRange() throws Exception {

        BookingCreateByCarDto request = new BookingCreateByCarDto();
        request.setClientId(testClient.getId());
        request.setCarId(testCar.getId());
        request.setPickupLocationId(testLocation.getId());
        request.setReturnLocationId(testLocation.getId());
        request.setPickup(Instant.now().plus(3, ChronoUnit.DAYS));
        request.setRet(Instant.now().plus(1, ChronoUnit.DAYS)); // Return before pickup


        mockMvc.perform(post("/api/bookings/by-car")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Return date must be after pickup date"));
    }

    @Test
    void createBookingByCar_ShouldFail_WhenCarNotFound() throws Exception {

        BookingCreateByCarDto request = new BookingCreateByCarDto();
        request.setClientId(testClient.getId());
        request.setCarId(99999L); //
        request.setPickupLocationId(testLocation.getId());
        request.setReturnLocationId(testLocation.getId());
        request.setPickup(Instant.now().plus(1, ChronoUnit.DAYS));
        request.setRet(Instant.now().plus(3, ChronoUnit.DAYS));


        mockMvc.perform(post("/api/bookings/by-car")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void createBookingByCar_ShouldFail_WhenMissingRequiredFields() throws Exception {

        BookingCreateByCarDto request = new BookingCreateByCarDto();

        mockMvc.perform(post("/api/bookings/by-car")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void getActiveBookingsForCompany_ShouldReturnEmptyList_WhenNoBookings() throws Exception {

        mockMvc.perform(get("/api/bookings/company/{companyId}/active", testCar.getCompany().getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
