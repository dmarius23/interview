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

@RestController
@RequestMapping("/api/cars")
@Validated
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    @PostMapping
    public CarResponseDto add(@Valid @RequestBody CarCreateDto req) {
        return carService.addCar(req);
    }

    @PutMapping("/{id}")
    public CarResponseDto update(@PathVariable Long id, @Valid @RequestBody CarUpdateDto req) {
        return carService.updateCar(id, req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponseDto> getById(@PathVariable Long id) {
        Optional<CarResponseDto> car = carService.findById(id);
        return car.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-company")
    public PageResponse<CarResponseDto> getByCompany(@RequestParam Long companyId, Pageable pageable) {
        return carService.findByCompanyId(companyId, pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        carService.deleteCar(id);
    }
}