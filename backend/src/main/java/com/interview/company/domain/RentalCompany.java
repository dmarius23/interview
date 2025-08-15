package com.interview.company.domain;

import com.interview.common.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "rental_company")
@SQLDelete(sql =
        "UPDATE rental_company " +
                "SET deleted = TRUE, deleted_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND version = ?")
@Where(clause = "deleted = false")  //TODO what happen if I want to get the deleted once?
@Getter
@Setter
public class RentalCompany extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

//    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    private List<RentalLocation> locations = new ArrayList<>();
//
//    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    private List<Car> cars = new ArrayList<>();

    @Column(name = "deleted", nullable = false, columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
