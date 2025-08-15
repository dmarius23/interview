package com.interview.company.repo;

import com.interview.company.domain.RentalLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalLocationRepository extends JpaRepository<RentalLocation, Long> {
    Page<RentalLocation> findByCompanyId(Long companyId, Pageable pageable);

    @EntityGraph(attributePaths = {"company"})
    Page<RentalLocation> findByCityIgnoreCase(String city, Pageable pageable);
}

