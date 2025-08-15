package com.interview.fleet.repo;

import com.interview.booking.domain.BookingStatus;
import com.interview.fleet.domain.Car;
import com.interview.fleet.repo.dto.CarInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CarAvailabilityRepository extends Repository<Car, Long> {

    // Distinct CarModel IDs available at a given location for a company in the time window
    @Query(
            "select distinct m.id " +
                    "from com.interview.fleet.domain.Car c " +
                    "join c.model m " +
                    "join c.company co " +
                    "join c.currentLocation loc " +
                    "where co.id = :companyId and loc.id = :locationId " +
                    "  and c.status = com.interview.fleet.domain.CarStatus.MAINTENANCE " +
                    "  and not exists (" +
                    "       select b.id from com.interview.booking.domain.Booking b " +
                    "       where b.car = c " +
                    "         and b.status in (:active) " +
                    "         and b.pickupTime < :to and b.returnTime > :from" +
                    "  )"
    )
    Page<Long> findAvailableModelIdsAtLocationForCompany(@Param("companyId") Long companyId,
                                                         @Param("locationId") Long locationId,
                                                         @Param("from") Instant from,
                                                         @Param("to") Instant to,
                                                         @Param("active") Set<BookingStatus> active,
                                                         Pageable pageable);

    // Distinct CarModel IDs available in a city for a company in the time window
    @Query(
            "select distinct m.id " +
                    "from com.interview.fleet.domain.Car c " +
                    "join c.model m " +
                    "join c.company co " +
                    "join c.currentLocation loc " +
                    "where co.id = :companyId and lower(loc.city) = lower(:city) " +
                    "  and c.status = com.interview.fleet.domain.CarStatus.MAINTENANCE " +
                    "  and not exists (" +
                    "       select b.id from com.interview.booking.domain.Booking b " +
                    "       where b.car = c " +
                    "         and b.status in (:active) " +
                    "         and b.pickupTime < :to and b.returnTime > :from" +
                    "  )"
    )
    Page<Long> findAvailableModelIdsInCityForCompany(@Param("companyId") Long companyId,
                                                     @Param("city") String city,
                                                     @Param("from") Instant from,
                                                     @Param("to") Instant to,
                                                     @Param("active") Set<BookingStatus> active,
                                                     Pageable pageable);


    @Query(" select c from Car c " +
            "join c.model m " +
            "join c.currentLocation loc " +
            "where m.id = :modelId " +
            "and loc.id = :locationId " +
            "and c.status = com.interview.fleet.domain.CarStatus.MAINTENANCE " +
            "and not exists ( " +
            "select b.id from Booking b " +
            "where b.car = c " +
            "and b.status in :activeStatuses " +
            "and b.pickupTime < :to " +
            "and b.returnTime > :from) " +
            "order by c.plateNumber")
    Page<Car> findAvailableCarsByModelAtLocation(
            @Param("modelId") Long modelId,
            @Param("locationId") Long locationId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("activeStatuses") Set<BookingStatus> activeStatuses,
            Pageable pageable
    );


    // Search available cars with constructor projection + pagination
    // Availability rule: cars with no overlapping active bookings
    @Query(value = "select new com.interview.fleet.repo.dto.CarInfoDto( " +
            "c.id, c.plateNumber, c.status, " +
            "m.make, m.model, co.name, loc.name) " +
            "from Car c " +
            "join c.model m " +
            "join c.company co " +
            "left join c.currentLocation loc " +
            "where (:locationId is null or loc.id = :locationId) " +
            "and c.status <> com.interview.fleet.domain.CarStatus.MAINTENANCE " +
            "and not exists ( " +
            "select b.id from com.interview.booking.domain.Booking b  " +
            "where b.car = c " +
            "and b.status in (:activeStatuses) " +
            "and b.pickupTime < :to and b.returnTime > :from) " +
            "order by m.make, m.model, c.plateNumber ",
            countQuery = "select count(c) from Car c " +
                    "left join c.currentLocation loc " +
                    "where (:locationId is null or loc.id = :locationId) " +
                    "and c.status <> com.interview.fleet.domain.CarStatus.MAINTENANCE " +
                    "and not exists (" +
                    "select b.id from com.interview.booking.domain.Booking b " +
                    "where b.car = c " +
                    "and b.status in (:activeStatuses) " +
                    "and b.pickupTime < :to and b.returnTime > :from) "
    )
    Page<CarInfoDto> findAvailableCars(
            @Param("locationId") Long locationId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("activeStatuses") Set<BookingStatus> BookingStatus,
            Pageable pageable);

    @Query(
            "select c.id " +
                    "from Car c " +
                    "join c.model m " +
                    "left join c.currentLocation loc " +
                    "where m.id = :modelId " +
                    "and (:locationId is null or loc.id = :locationId) " +
                    "  and c.status = com.interview.fleet.domain.CarStatus.MAINTENANCE " +
                    "  and not exists (" +
                    "      select b.id from Booking b " +
                    "      where b.car = c " +
                    "        and b.status in (:activeStatuses) " +
                    "        and b.pickupTime < :to and b.returnTime > :from" +
                    "  ) " +
                    "order by c.id asc"
    )
    List<Long> findAvailableCarIds(@Param("modelId") Long modelId,
                                   @Param("locationId") Long locationId,
                                   @Param("from") Instant from,
                                   @Param("to") Instant to,
                                   @Param("activeStatuses") Set<BookingStatus> activeStatuses,
                                   Pageable limit);


    @Query("select c from Car c " +
            "join c.currentLocation loc " +
            "where lower(loc.city) = lower(:city) " +
            "and c.status = com.interview.fleet.domain.CarStatus.MAINTENANCE " +
            "and not exists ( " +
            "   select b.id from com.interview.booking.domain.Booking b " +
            "   where b.car = c " +
            "     and b.status in (:activeStatuses) " +
            "     and b.pickupTime < :to and b.returnTime > :from " +
            ")")
    Page<Car> findAvailableCarsByCity(@Param("city") String city,
                                      @Param("from") Instant from,
                                      @Param("to") Instant to,
                                      @Param("activeStatuses") Set<BookingStatus> activeStatuses,
                                      Pageable pageable);


    @Query("select (count(b) > 0) from Booking b " +
            "where b.car.id = :carId " +
            "and b.status in :statuses " +
            "and b.pickupTime < :to " +
            "and b.returnTime > :from "
    )
    boolean existsActiveForCarInPeriod(@Param("carId") Long carId,
                                       @Param("from") Instant from,
                                       @Param("to") Instant to,
                                       @Param("statuses") Collection<BookingStatus> statuses);

    @Query("select (count(b) > 0) from Booking b " +
            "where b.car.id = :carId " +
            "  and b.status in (:statuses) " +
            "  and b.returnTime > :now")
    boolean hasActiveOrUpcomingForCar(@Param("carId") Long carId,
                                      @Param("now") Instant now,
                                      @Param("statuses") Collection<BookingStatus> statuses);


}