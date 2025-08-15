package com.interview.company;

import com.interview.common.BaseIntegrationTest;
import com.interview.company.domain.RentalCompany;
import com.interview.company.domain.RentalLocation;
import com.interview.company.dto.RentalLocationCreateDto;
import com.interview.company.dto.RentalLocationUpdateDto;
import com.interview.company.repo.RentalCompanyRepository;
import com.interview.company.repo.RentalLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RentalLocationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RentalCompanyRepository companyRepository;

    @Autowired
    private RentalLocationRepository locationRepository;

    private RentalCompany testCompany;
    private RentalLocation testLocation;

    @BeforeEach
    void setUp() {

        testCompany = new RentalCompany();
        testCompany.setName("Test Rental Company");
        testCompany = companyRepository.saveAndFlush(testCompany);


        testLocation = new RentalLocation();
        testLocation.setCompany(testCompany);
        testLocation.setName("Test Location");
        testLocation.setCity("Test City");
        testLocation.setCountry("Test Country");
        testLocation = locationRepository.saveAndFlush(testLocation);
    }

    @Test
    void createLocation_ShouldSucceed_WhenValidRequest() throws Exception {

        RentalLocationCreateDto request = new RentalLocationCreateDto();
        request.setCompanyId(testCompany.getId());
        request.setName("New Location");
        request.setCity("New City");
        request.setCountry("New Country");


        mockMvc.perform(post("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Location"))
                .andExpect(jsonPath("$.city").value("New City"))
                .andExpect(jsonPath("$.country").value("New Country"));
    }

    @Test
    void createLocation_ShouldFail_WhenMissingRequiredFields() throws Exception {

        RentalLocationCreateDto request = new RentalLocationCreateDto();

        mockMvc.perform(post("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createLocation_ShouldFail_WhenInvalidCompanyId() throws Exception {

        RentalLocationCreateDto request = new RentalLocationCreateDto();
        request.setCompanyId(99999L); // Non-existent company
        request.setName("New Location");
        request.setCity("New City");
        request.setCountry("New Country");


        mockMvc.perform(post("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getLocationById_ShouldReturnLocation_WhenExists() throws Exception {

        mockMvc.perform(get("/api/locations/{id}", testLocation.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLocation.getId()))
                .andExpect(jsonPath("$.name").value(testLocation.getName()))
                .andExpect(jsonPath("$.city").value(testLocation.getCity()))
                .andExpect(jsonPath("$.country").value(testLocation.getCountry()));
    }

    @Test
    void getLocationById_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {

        mockMvc.perform(get("/api/locations/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void updateLocation_ShouldSucceed_WhenValidRequest() throws Exception {

        RentalLocationUpdateDto request = new RentalLocationUpdateDto();
        request.setName("Updated Location Name");
        request.setCity("Updated City");
        request.setCountry("Updated Country");
        request.setVersion(testLocation.getVersion());

        mockMvc.perform(put("/api/locations/{id}", testLocation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testLocation.getId()))
                .andExpect(jsonPath("$.name").value("Updated Location Name"))
                .andExpect(jsonPath("$.city").value("Updated City"))
                .andExpect(jsonPath("$.country").value("Updated Country"));
    }

    @Test
    void updateLocation_ShouldFail_WhenLocationNotFound() throws Exception {

        RentalLocationUpdateDto request = new RentalLocationUpdateDto();
        request.setName("Updated Name");
        request.setCity("Updated City");
        request.setVersion(0L);


        mockMvc.perform(put("/api/locations/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getLocationsByCity_ShouldReturnPagedResults() throws Exception {

        mockMvc.perform(get("/api/locations/byCity")
                .param("city", testLocation.getCity())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(testLocation.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getLocationsByCompany_ShouldReturnPagedResults() throws Exception {

        mockMvc.perform(get("/api/locations/byCompany")
                .param("companyId", testCompany.getId().toString())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(testLocation.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getLocationsByCity_ShouldReturnEmpty_WhenNoCityMatch() throws Exception {

        mockMvc.perform(get("/api/locations/byCity")
                .param("city", "Nonexistent City")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getLocationsByCompany_ShouldReturnEmpty_WhenNoCompanyMatch() throws Exception {

        mockMvc.perform(get("/api/locations/byCompany")
                .param("companyId", "99999")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void deleteLocation_ShouldSucceed_WhenNoActiveBookings() throws Exception {

        mockMvc.perform(delete("/api/locations/{id}", testLocation.getId()))
                .andExpect(status().isOk());


        mockMvc.perform(get("/api/locations/{id}", testLocation.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteLocation_ShouldFail_WhenLocationNotFound() throws Exception {

        mockMvc.perform(delete("/api/locations/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
