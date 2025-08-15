package com.interview.catalog.web;

import com.interview.catalog.dto.CarModelCreateDto;
import com.interview.catalog.dto.CarModelResponseDto;
import com.interview.catalog.dto.CarModelUpdateDto;
import com.interview.catalog.service.impl.CarModelServiceImpl;
import com.interview.common.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/carmodels")
@Validated
@RequiredArgsConstructor
public class CarModelController {
    private final CarModelServiceImpl carModelServiceImpl;

    @PostMapping
    public CarModelResponseDto create(@Valid @RequestBody CarModelCreateDto req) {
        return carModelServiceImpl.create(req);
    }

    @GetMapping("/{id}")
    public CarModelResponseDto get(@PathVariable Long id) {
        return carModelServiceImpl.get(id);
    }

    @GetMapping("/byMake")
    public PageResponse<CarModelResponseDto> listByMake(@RequestParam String make, Pageable pageable) {
        return carModelServiceImpl.listByMake(make, pageable);
    }

    @GetMapping
    public PageResponse<CarModelResponseDto> getAllModels(Pageable pageable) {
        return carModelServiceImpl.getAllModels(pageable);
    }

    @PutMapping("/{id}")
    public CarModelResponseDto update(@Valid @RequestBody CarModelUpdateDto req, @PathVariable Long id) {
        return carModelServiceImpl.update(req, id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        carModelServiceImpl.delete(id);
    }
}