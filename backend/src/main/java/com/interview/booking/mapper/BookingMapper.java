package com.interview.booking.mapper;

import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.dto.BookingResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "carId", source = "car.id")
    @Mapping(target = "pickup", source = "pickupTime")
    @Mapping(target = "ret", source = "returnTime")
    @Mapping(target = "pickupLocationId", source = "pickupLocation.id")
    @Mapping(target = "returnLocationId", source = "returnLocation.id")
    @Mapping(target = "totalPriceCents", source = "totalPriceCents")
    @Mapping(target = "carModelId", expression = "java(booking.getCar() != null && booking.getCar().getModel() != null ? booking.getCar().getModel().getId() : null)")
    BookingResponseDto toResponse(Booking booking);

    @Named("statusToString")
    default String statusToString(BookingStatus status) {
        return status != null ? status.name() : null;
    }

}
