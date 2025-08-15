package com.interview.fleet.web;

import com.interview.common.web.PageResponse;
import com.interview.fleet.dtos.CarCreateDto;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.dtos.CarUpdateDto;
import com.interview.fleet.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

/**
 * REST controller for car management operations.
 */
@RestController
@RequestMapping("/api/cars")
@Validated
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    /**
     * Add a new car to the fleet.
     */
    @PostMapping
    public CarResponseDto add(@Valid @RequestBody CarCreateDto req) {
        return carService.addCar(req);
    }

    /**
     * Update an existing car's details.
     */
    @PutMapping("/{id}")
    public CarResponseDto update(@PathVariable Long id, @Valid @RequestBody CarUpdateDto req) {
        return carService.updateCar(id, req);
    }

    /**
     * Get car details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CarResponseDto> getById(@PathVariable Long id) {
        Optional<CarResponseDto> car = carService.findById(id);
        return car.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all cars belonging to a specific company.
     */
    @GetMapping("/by-company")
    public PageResponse<CarResponseDto> getByCompany(@RequestParam Long companyId, Pageable pageable) {
        return carService.findByCompanyId(companyId, pageable);
    }

    /**
     * Delete a car from the fleet.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        carService.deleteCar(id);
    }
}