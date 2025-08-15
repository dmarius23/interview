package com.interview.fleet;

import com.interview.catalog.domain.CarModel;
import com.interview.catalog.repo.CarModelRepository;
import com.interview.common.BaseIntegrationTest;
import com.interview.common.TestDataFactory;
import com.interview.company.domain.RentalCompany;
import com.interview.company.domain.RentalLocation;
import com.interview.company.repo.RentalCompanyRepository;
import com.interview.company.repo.RentalLocationRepository;
import com.interview.fleet.domain.Car;
import com.interview.fleet.dtos.CarCreateDto;
import com.interview.fleet.dtos.CarUpdateDto;
import com.interview.fleet.repo.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CarControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private RentalCompanyRepository companyRepository;

    @Autowired
    private RentalLocationRepository locationRepository;

    private RentalCompany testCompany;
    private RentalLocation testLocation;
    private CarModel testCarModel;
    private Car testCar;

    @BeforeEach
    void setUp() {
        // Create and persist test data in the correct order
        testCompany = TestDataFactory.createTestCompany();
        testCompany = companyRepository.saveAndFlush(testCompany);

        testLocation = TestDataFactory.createTestLocation(testCompany);
        testLocation = locationRepository.saveAndFlush(testLocation);

        testCarModel = TestDataFactory.createTestCarModel();
        testCarModel = carModelRepository.saveAndFlush(testCarModel);

        testCar = TestDataFactory.createTestCar(testCompany, testCarModel, testLocation);
        testCar = carRepository.saveAndFlush(testCar);
    }

    @Test
    void addCar_ShouldSucceed_WhenValidRequest() throws Exception {
        // Given
        CarCreateDto request = new CarCreateDto();
        request.setCompanyId(testCompany.getId());
        request.setModelId(testCarModel.getId());
        request.setCurrentLocationId(testLocation.getId());
        request.setVin("NEW-VIN-123456");
        request.setPlateNumber("NEW-PLATE-123");
        request.setMileageKm(0);
        request.setPricePerDay(6000);

        // When & Then
        mockMvc.perform(post("/api/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.vin").value("NEW-VIN-123456"))
                .andExpect(jsonPath("$.plateNumber").value("NEW-PLATE-123"))
                .andExpect(jsonPath("$.pricePerDay").value(6000))
                .andExpect(jsonPath("$.companyId").value(testCompany.getId()))
                .andExpect(jsonPath("$.modelId").value(testCarModel.getId()));
    }

    @Test
    void addCar_ShouldFail_WhenMissingRequiredFields() throws Exception {
        // Given
        CarCreateDto request = new CarCreateDto();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void addCar_ShouldFail_WhenInvalidCompanyId() throws Exception {
        // Given
        CarCreateDto request = new CarCreateDto();
        request.setCompanyId(99999L); // Non-existent company
        request.setModelId(testCarModel.getId());
        request.setVin("NEW-VIN-123456");
        request.setPlateNumber("NEW-PLATE-123");
        request.setPricePerDay(6000);

        // When & Then
        mockMvc.perform(post("/api/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isNotFound()); // Due to FK constraint or entity not found
    }

    @Test
    void addCar_ShouldFail_WhenDuplicateVin() throws Exception {
        // Given
        CarCreateDto request = new CarCreateDto();
        request.setCompanyId(testCompany.getId());
        request.setModelId(testCarModel.getId());
        request.setCurrentLocationId(testLocation.getId());
        request.setVin(testCar.getVin()); // Duplicate VIN
        request.setPlateNumber("DIFFERENT-PLATE");
        request.setPricePerDay(6000);

        // When & Then
        mockMvc.perform(post("/api/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_VIOLATION"));
    }

    @Test
    void addCar_ShouldFail_WhenDuplicatePlateNumber() throws Exception {
        // Given
        CarCreateDto request = new CarCreateDto();
        request.setCompanyId(testCompany.getId());
        request.setModelId(testCarModel.getId());
        request.setCurrentLocationId(testLocation.getId());
        request.setVin("DIFFERENT-VIN-123");
        request.setPlateNumber(testCar.getPlateNumber()); // Duplicate plate
        request.setPricePerDay(6000);

        // When & Then
        mockMvc.perform(post("/api/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_VIOLATION"));
    }

    @Test
    void getCarById_ShouldReturnCar_WhenCarExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/cars/{id}", testCar.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testCar.getId()))
                .andExpect(jsonPath("$.plateNumber").value(testCar.getPlateNumber()))
                .andExpect(jsonPath("$.vin").value(testCar.getVin()));
    }

    @Test
    void getCarById_ShouldReturnNotFound_WhenCarDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/cars/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCar_ShouldSucceed_WhenValidRequest() throws Exception {
        // Given
        CarUpdateDto request = new CarUpdateDto();
        request.setId(testCar.getId());
        request.setMileageKm(15000);
        request.setPricePerDay(5500);

        // When & Then
        mockMvc.perform(put("/api/cars/{id}", testCar.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testCar.getId()))
                .andExpect(jsonPath("$.mileageKm").value(15000))
                .andExpect(jsonPath("$.pricePerDay").value(5500));
    }

    @Test
    void updateCar_ShouldFail_WhenCarNotFound() throws Exception {
        // Given
        CarUpdateDto request = new CarUpdateDto();
        request.setMileageKm(15000);

        // When & Then
        mockMvc.perform(put("/api/cars/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getCarsByCompany_ShouldReturnPagedResults() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/cars/by-company")
                .param("companyId", testCompany.getId().toString())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(testCar.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void deleteCar_ShouldSucceed_WhenNoActiveBookings() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/cars/{id}", testCar.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cars/{id}", testCar.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCar_ShouldFail_WhenCarNotFound() throws Exception {

        mockMvc.perform(delete("/api/cars/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}