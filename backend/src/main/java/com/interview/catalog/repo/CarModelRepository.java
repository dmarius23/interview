package com.interview.catalog.repo;


import com.interview.catalog.domain.CarModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CarModelRepository extends JpaRepository<CarModel, Long> {

    Page<CarModel> findByMakeIgnoreCase(String make, Pageable pageable);

    Page<CarModelView> findByMakeIgnoreCaseOrderByModelAsc(String make, Pageable pageable);
}

