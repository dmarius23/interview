package com.interview.company;

import com.interview.common.BaseIntegrationTest;
import com.interview.company.domain.RentalCompany;
import com.interview.company.dto.RentalCompanyCreateDto;
import com.interview.company.dto.RentalCompanyUpdateDto;
import com.interview.company.repo.RentalCompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RentalCompanyControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RentalCompanyRepository companyRepository;

    private RentalCompany testCompany;

    @BeforeEach
    void setUp() {
        testCompany = new RentalCompany();
        testCompany.setName("Test Rental Company");
        testCompany = companyRepository.saveAndFlush(testCompany);
    }

    @Test
    void createCompany_ShouldSucceed_WhenValidRequest() throws Exception {
        // Given
        RentalCompanyCreateDto request = new RentalCompanyCreateDto();
        request.setName("New Rental Company");

        // When & Then
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Rental Company"));
    }

    @Test
    void createCompany_ShouldFail_WhenMissingName() throws Exception {
        // Given
        RentalCompanyCreateDto request = new RentalCompanyCreateDto();
        // Missing name

        // When & Then
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void getCompany_ShouldReturnCompany_WhenExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/companies/{id}", testCompany.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testCompany.getId()))
                .andExpect(jsonPath("$.name").value(testCompany.getName()));
    }

    @Test
    void getCompany_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/companies/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void updateCompany_ShouldSucceed_WhenValidRequest() throws Exception {
        // Given
        RentalCompanyUpdateDto request = new RentalCompanyUpdateDto();
        request.setName("Updated Company Name");
        request.setVersion(testCompany.getVersion());

        // When & Then
        mockMvc.perform(put("/api/companies/{id}", testCompany.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testCompany.getId()))
                .andExpect(jsonPath("$.name").value("Updated Company Name"));
    }

    @Test
    void updateCompany_ShouldFail_WhenCompanyNotFound() throws Exception {
        // Given
        RentalCompanyUpdateDto request = new RentalCompanyUpdateDto();
        request.setName("Updated Name");
        request.setVersion(0L);

        // When & Then
        mockMvc.perform(put("/api/companies/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getAllCompanies_ShouldReturnPagedResults() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/companies")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(testCompany.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void deleteCompany_ShouldSucceed_WhenNoActiveBookings() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/companies/{id}", testCompany.getId()))
                .andExpect(status().isOk());

        // Verify company is soft deleted
        mockMvc.perform(get("/api/companies/{id}", testCompany.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCompany_ShouldFail_WhenCompanyNotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/companies/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
