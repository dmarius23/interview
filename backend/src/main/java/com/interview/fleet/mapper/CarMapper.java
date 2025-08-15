package com.interview.fleet.mapper;

import com.interview.fleet.domain.Car;
import com.interview.fleet.dtos.CarCreateDto;
import com.interview.fleet.dtos.CarResponseDto;
import com.interview.fleet.dtos.CarUpdateDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CarMapper {
    @Mappings({
            @Mapping(target = "company.id", source = "companyId"),
            @Mapping(target = "model.id", source = "modelId"),
            @Mapping(target = "currentLocation.id", source = "currentLocationId")
    })
    Car toEntity(CarCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "currentLocation.id", source = "currentLocationId", ignore = true) // handled in service if present
    })
    void updateEntityFromDto(CarUpdateDto dto, @MappingTarget Car entity);

    @Mappings({
            @Mapping(target = "companyId", source = "company.id"),
            @Mapping(target = "modelId", source = "model.id"),
            @Mapping(target = "currentLocationId", source = "currentLocation.id")
    })
    CarResponseDto toResponse(Car entity);
}

