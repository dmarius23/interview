package com.interview.catalog.mapper;

import com.interview.catalog.domain.CarModel;
import com.interview.catalog.dto.CarModelCreateDto;
import com.interview.catalog.dto.CarModelResponseDto;
import com.interview.catalog.dto.CarModelUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CarModelMapper {
    CarModel toEntity(CarModelCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CarModelUpdateDto dto, @MappingTarget CarModel entity);

    CarModelResponseDto toResponse(CarModel entity);
}
