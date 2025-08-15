package com.interview.fleet.domain;

import com.interview.catalog.domain.CarModel;
import com.interview.common.domain.BaseEntity;
import com.interview.company.domain.RentalCompany;
import com.interview.company.domain.RentalLocation;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;


@Entity
@Getter
@Setter
@Table(name = "car")
@SQLDelete(sql = "UPDATE car SET deleted = true, deleted_at = now() WHERE id = ? and version = ?")
@Where(clause = "deleted = false")
public class Car extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version; // kept for optimistic locking on status/mileage

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private RentalCompany company;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private CarModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_location_id")
    private RentalLocation currentLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CarStatus status = CarStatus.AVAILABLE;

    @Column(nullable = false, unique = true, length = 40)
    private String vin;

    @Column(nullable = false, unique = true, length = 20)
    private String plateNumber;

    private Integer mileageKm;

    private Integer dailyPriceInCents;

    @Column(name = "deleted", nullable = false, columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
