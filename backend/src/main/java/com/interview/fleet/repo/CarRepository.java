package com.interview.fleet.repo;

import com.interview.fleet.domain.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {


    @EntityGraph(attributePaths = {"model", "company", "currentLocation"})
    Optional<Car> findById(Long id);

    @EntityGraph(attributePaths = {"model", "currentLocation"})
    Page<Car> findByCompanyId(Long companyId, Pageable pageable);

//    @EntityGraph(attributePaths = {"model", "currentLocation"})
//    Page<Car> findByCompanyIdAndLocation(Long companyId, Long currentLocationId, Pageable pageable);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "javax.persistence.lock.timeout", value = "2000")) // 2s timeout
    @Query("select c from Car c where c.id = :id")
    Optional<Car> lockByIdForUpdate(@Param("id") Long id);

}
