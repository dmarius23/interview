package com.interview.booking.repo;

import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.repo.dto.BookingInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"car", "car.model", "pickupLocation", "returnLocation", "payment"})
    Optional<Booking> findById(Long id);

    @Query(value = "select new com.interview.booking.repo.dto.BookingInfoDto( " +
            "b.id, b.status, b.pickupTime, b.returnTime, b.totalPriceCents, " +
            "c.plateNumber, m.make, m.model, " +
            "pl.name, rl.name, p.status) " +
            "from Booking b " +
            "join b.car c " +
            "join c.model m " +
            "join b.pickupLocation pl " +
            "join b.returnLocation rl " +
            "left join b.payment p " +
            "where b.client.id = :clientId " +
            "order by b.pickupTime desc ",
            countQuery = "select count(b) from Booking b where b.client.id = :clientId"
    )
    Page<BookingInfoDto> findListByClientId(@Param("clientId") Long clientId, Pageable pageable);


    @Query("select (count(b) > 0) from Booking b " +
            "where b.car.company.id = :companyId " +
            "  and b.status in (:active) " +
            "  and b.pickupTime <= :now and b.returnTime > :now")
    boolean hasOngoingForCompany(@Param("companyId") Long companyId,
                                 @Param("now") Instant now,
                                 @Param("active") Collection<BookingStatus> active);


    @Query("select (count(b) > 0) from Booking b " +
            "where (b.pickupLocation.id = :locationId or b.returnLocation.id = :locationId) " +
            "  and b.status in (:active) " +
            "  and b.pickupTime <= :now and b.returnTime > :now")
    boolean hasOngoingForLocation(@Param("locationId") Long locationId,
                                  @Param("now") Instant now,
                                  @Param("active") Collection<BookingStatus> active);

    @Query("select b from Booking b " +
            "where b.car.company.id = :companyId " +
            "  and b.status in (:active) " +
            "  and b.pickupTime > :now")
    List<Booking> findFutureActiveForCompany(@Param("companyId") Long companyId,
                                             @Param("now") Instant now,
                                             @Param("active") Collection<BookingStatus> active);

    @Query("select b from Booking b " +
            "where (b.pickupLocation.id = :locationId or b.returnLocation.id = :locationId) " +
            "  and b.status in (:active) " +
            "  and b.pickupTime > :now")
    List<Booking> findFutureActiveForLocation(@Param("locationId") Long locationId,
                                              @Param("now") Instant now,
                                              @Param("active") Collection<BookingStatus> active);


    /**
     * Find all active bookings for a company (bookings that are started or in the future).
     * Active bookings are those that are not CANCELED or COMPLETED and have not ended yet.
     */
    @Query("SELECT b FROM Booking b " +
            "JOIN b.car c " +
            "JOIN c.currentLocation l " +
            "WHERE l.company.id = :companyId " +
            "AND b.status NOT IN ('CANCELED', 'COMPLETED') " +
            "AND b.returnTime >= :now " +
            "ORDER BY b.pickupTime ASC")
    List<Booking> findActiveBookingsForCompany(@Param("companyId") Long companyId, @Param("now") Instant now);

    /**
     * Find all active bookings for a specific location (active and future bookings).
     * This includes bookings where either pickup or return location matches.
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE (b.pickupLocation.id = :locationId OR b.returnLocation.id = :locationId) " +
            "AND b.status NOT IN ('CANCELED', 'COMPLETED') " +
            "AND b.returnTime >= :now " +
            "ORDER BY b.pickupTime ASC")
    List<Booking> findActiveBookingsForLocation(@Param("locationId") Long locationId, @Param("now") Instant now);

    /**
     * Find all bookings for a location that overlap with the specified time period.
     * A booking overlaps if it starts before the period ends and ends after the period starts.
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE (b.pickupLocation.id = :locationId OR b.returnLocation.id = :locationId) " +
            "AND b.pickupTime < :endTime " +
            "AND b.returnTime > :startTime " +
            "ORDER BY b.pickupTime ASC")
    List<Booking> findBookingsForLocationInPeriod(@Param("locationId") Long locationId,
                                                  @Param("startTime") Instant startTime,
                                                  @Param("endTime") Instant endTime);


}

