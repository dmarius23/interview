package com.interview.catalog.service.impl;

import com.interview.catalog.domain.CarModel;
import com.interview.catalog.dto.CarModelCreateDto;
import com.interview.catalog.dto.CarModelResponseDto;
import com.interview.catalog.dto.CarModelUpdateDto;
import com.interview.catalog.mapper.CarModelMapper;
import com.interview.catalog.repo.CarModelRepository;
import com.interview.catalog.service.CarModelService;
import com.interview.common.annotation.Loggable;
import com.interview.common.domain.EntityNotFound;
import com.interview.common.mapper.PageResponseMapper;
import com.interview.common.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Loggable(logParams = true, logResult = false)
@RequiredArgsConstructor
public class CarModelServiceImpl implements CarModelService {
    public static final String CAR_MODEL_BY_ID = "carModelById";
    public static final String CAR_MODELS_BY_MAKE = "carModelsByMake";
    public static final String ALL_CAR_MODELS = "allCarModels";

    private final CarModelRepository carModelRepository;
    private final CarModelMapper carModelMapper;
    private final PageResponseMapper pageResponseMapper;


    @Transactional(readOnly = true)
    @Cacheable("carModelById")
    public CarModelResponseDto get(Long id) {
        CarModel carModel = carModelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFound("CarModel not found: " + id));
        return carModelMapper.toResponse(carModel);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "carModelsByMake",
            key = "T(java.util.Objects).hash(#make, #pageable.pageNumber, #pageable.pageSize, #pageable.sort)")
    public PageResponse<CarModelResponseDto> listByMake(String make, Pageable pageable) {
        Page<CarModel> page = carModelRepository.findByMakeIgnoreCase(make, pageable);
        return pageResponseMapper.toPageResponse(page, carModelMapper::toResponse);
    }

    @Transactional
    @CachePut(value = CAR_MODEL_BY_ID, key = "#result.id", condition = "#result != null")
    @CacheEvict(value = CAR_MODELS_BY_MAKE, allEntries = true)
    public CarModelResponseDto create(CarModelCreateDto createDto) {
        CarModel carModel = carModelMapper.toEntity(createDto);
        CarModel saved = carModelRepository.save(carModel);
        return carModelMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ALL_CAR_MODELS)
    public PageResponse<CarModelResponseDto> getAllModels(Pageable pageable) {
        Page<CarModel> page = carModelRepository.findAll(pageable);
        return pageResponseMapper.toPageResponse(page, carModelMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = CAR_MODELS_BY_MAKE, allEntries = true)
    @CachePut(value = CAR_MODEL_BY_ID, key = "#result.id")
    public CarModelResponseDto update(CarModelUpdateDto updateDto, Long id) {
        CarModel existingModel = carModelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFound("CarModel not found: " + id));

        carModelMapper.updateEntityFromDto(updateDto, existingModel);
        CarModel updated = carModelRepository.save(existingModel);
        return carModelMapper.toResponse(updated);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CAR_MODEL_BY_ID, key = "#id"),
            @CacheEvict(value = CAR_MODELS_BY_MAKE, allEntries = true)
    })
    public void delete(Long id) {
        carModelRepository.deleteById(id);
    }
}