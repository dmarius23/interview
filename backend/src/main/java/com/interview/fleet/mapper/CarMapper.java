package com.interview.fleet.mapper;

import com.interview.fleet.domain.Car;
import com.interview.fleet.dtos.CarCreateDto;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.dtos.CarUpdateDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CarMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "company", ignore = true), // Will be set manually in service
            @Mapping(target = "model", ignore = true),   // Will be set manually in service
            @Mapping(target = "currentLocation", ignore = true), // Will be set manually in service
            @Mapping(target = "status", constant = "AVAILABLE"),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "updatedDate", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "dailyPriceInCents", source = "pricePerDay")
    })
    Car toEntity(CarCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "company", ignore = true),
            @Mapping(target = "model", ignore = true),
            @Mapping(target = "currentLocation", ignore = true), // Handled manually in service if present
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "updatedDate", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "dailyPriceInCents", source = "pricePerDay")
    })
    void updateEntityFromDto(CarUpdateDto dto, @MappingTarget Car entity);

    @Mappings({
            @Mapping(target = "companyId", source = "company.id"),
            @Mapping(target = "modelId", source = "model.id"),
            @Mapping(target = "currentLocationId", source = "currentLocation.id"),
            @Mapping(target = "pricePerDay", source = "dailyPriceInCents")
    })
    CarResponseDto toResponse(Car entity);
}

