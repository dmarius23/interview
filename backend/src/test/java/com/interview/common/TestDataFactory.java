package com.interview.common;

import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.dto.BookingCreateByCarDto;
import com.interview.catalog.domain.CarModel;
import com.interview.client.domain.Client;
import com.interview.company.domain.RentalCompany;
import com.interview.company.domain.RentalLocation;
import com.interview.fleet.domain.Car;
import com.interview.fleet.domain.CarStatus;
import com.interview.fleet.dtos.CarCreateDto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Factory class for creating test data objects
 */
public class TestDataFactory {

    public static RentalCompany createTestCompany() {
        RentalCompany company = new RentalCompany();
        company.setName("Test Rental Company");
        return company;
    }

    public static RentalLocation createTestLocation(RentalCompany company) {
        RentalLocation location = new RentalLocation();
        location.setCompany(company);
        location.setName("Test Location");
        location.setCity("Test City");
        location.setCountry("Test Country");
        return location;
    }

    public static CarModel createTestCarModel() {
        CarModel model = new CarModel();
        model.setMake("Toyota");
        model.setModel("Corolla");
        model.setVehicleClass("Compact");
        model.setSeats(5);
        return model;
    }

    public static Car createTestCar(RentalCompany company, CarModel model, RentalLocation location) {
        Car car = new Car();
        car.setCompany(company);
        car.setModel(model);
        car.setCurrentLocation(location);
        car.setVin("TEST-VIN-" + System.currentTimeMillis());
        car.setPlateNumber("TEST-" + System.currentTimeMillis());
        car.setMileageKm(10000);
        car.setDailyPriceInCents(5000);
        car.setStatus(CarStatus.AVAILABLE);
        return car;
    }

    public static Client createTestClient() {
        Client client = new Client();
        client.setFullName("Marius D");
        client.setEmail("marius.marius@test.com");
        client.setDriverLicenseNo("01CJ500");
        return client;
    }

    public static Booking createTestBooking(Client client, Car car, RentalLocation pickupLocation, RentalLocation returnLocation) {
        Booking booking = new Booking();
        booking.setClient(client);
        booking.setCar(car);
        booking.setPickupLocation(pickupLocation);
        booking.setReturnLocation(returnLocation);
        booking.setPickupTime(Instant.now().plus(1, ChronoUnit.DAYS));
        booking.setReturnTime(Instant.now().plus(3, ChronoUnit.DAYS));
        booking.setStatus(BookingStatus.CREATED);
        booking.setTotalPriceCents(10000); // $100.00
        return booking;
    }

    public static CarCreateDto createCarCreateDto(Long companyId, Long modelId, Long locationId) {
        CarCreateDto dto = new CarCreateDto();
        dto.setCompanyId(companyId);
        dto.setModelId(modelId);
        dto.setCurrentLocationId(locationId);
        dto.setVin("TEST-VIN-" + System.currentTimeMillis());
        dto.setPlateNumber("TEST-" + System.currentTimeMillis());
        dto.setMileageKm(0);
        dto.setPricePerDay(5000);
        return dto;
    }

    public static BookingCreateByCarDto createBookingCreateByCarDto(Long clientId, Long carId, Long locationId) {
        BookingCreateByCarDto dto = new BookingCreateByCarDto();
        dto.setClientId(clientId);
        dto.setCarId(carId);
        dto.setPickupLocationId(locationId);
        dto.setReturnLocationId(locationId);
        dto.setPickup(Instant.now().plus(1, ChronoUnit.DAYS));
        dto.setRet(Instant.now().plus(3, ChronoUnit.DAYS));
        return dto;
    }
}
