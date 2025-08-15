package com.interview.catalog.repo;


import com.interview.catalog.domain.CarModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// Option A: entity return + derived query
public interface CarModelRepository extends JpaRepository<CarModel, Long> {

    // Pageable + sort is handled by Pageable parameter
    Page<CarModel> findByMakeIgnoreCase(String make, Pageable pageable);

    // Option B (optional): projection for lighter reads
    interface CarModelView {
        Long getId();

        String getMake();

        String getModel();

        String getVehicleClass();

        Integer getSeats();
    }

    Page<CarModelView> findByMakeIgnoreCaseOrderByModelAsc(String make, Pageable pageable);
}

