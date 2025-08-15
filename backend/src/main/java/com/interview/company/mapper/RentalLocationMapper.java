package com.interview.company.mapper;

import com.interview.company.domain.RentalLocation;
import com.interview.company.dto.RentalLocationCreateDto;
import com.interview.company.dto.RentalLocationResponseDto;
import com.interview.company.dto.RentalLocationUpdateDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RentalLocationMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),
            @Mapping(target = "company", ignore = true)
    })
    RentalLocation toEntity(RentalLocationCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(RentalLocationUpdateDto dto, @MappingTarget RentalLocation entity);


    RentalLocationResponseDto toResponse(RentalLocation entity);
}
