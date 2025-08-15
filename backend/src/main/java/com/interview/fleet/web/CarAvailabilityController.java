package com.interview.fleet.web;

import com.interview.catalog.dto.CarModelResponseDto;
import com.interview.common.web.PageResponse;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.service.CarAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.Instant;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class CarAvailabilityController {
    private final CarAvailabilityService carAvailabilityService;

    @GetMapping("/models/by-location")
    public PageResponse<CarModelResponseDto> availableModelsAtLocationForCompany(
            @RequestParam @Positive Long companyId,
            @RequestParam @Positive Long locationId,
            @RequestParam("from") @NotNull Instant from,
            @RequestParam("to") @NotNull Instant to,
            Pageable pageable) {

        validateTimeRange(from, to);
        return carAvailabilityService.availableModelsAtLocationForCompany(companyId, locationId, from, to, pageable);
    }

    @GetMapping("/models/by-city-company")
    public PageResponse<CarModelResponseDto> availableModelsInCityForCompany(
            @RequestParam @Positive Long companyId,
            @RequestParam @NotBlank String city,
            @RequestParam("from") @NotNull Instant from,
            @RequestParam("to") @NotNull Instant to,
            Pageable pageable) {

        validateTimeRange(from, to);
        return carAvailabilityService.availableModelsInCityForCompany(companyId, city, from, to, pageable);
    }

    @GetMapping("/cars/by-model-at-location")
    public PageResponse<CarResponseDto> getAvailableCarsByModelAtLocation(
            @RequestParam @Positive Long modelId,
            @RequestParam @Positive Long locationId,
            @RequestParam("from") @NotNull Instant from,
            @RequestParam("to") @NotNull Instant to,
            Pageable pageable) {

        validateTimeRange(from, to);
        return carAvailabilityService.getAvailableCarsByModelAtLocation(modelId, locationId, from, to, pageable);
    }

    @GetMapping("/cars/by-city")
    public PageResponse<CarResponseDto> getAvailableCarsByCity(
            @RequestParam @NotBlank String city,
            @RequestParam("from") @NotNull Instant from,
            @RequestParam("to") @NotNull Instant to,
            Pageable pageable) {

        validateTimeRange(from, to);
        return carAvailabilityService.findAvailableCarsByCity(city, from, to, pageable);
    }

    private void validateTimeRange(Instant from, Instant to) {
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("'from' must be before 'to'");
        }
    }
}