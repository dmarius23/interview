package com.interview.company.repo;

import com.interview.company.domain.RentalCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RentalCompanyRepository extends JpaRepository<RentalCompany, Long> {
    Page<RentalCompany> findAll(Pageable page);

    Optional<RentalCompany> findById(Long id);

}
