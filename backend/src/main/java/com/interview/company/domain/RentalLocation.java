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
@Table(name = "rental_location")
@SQLDelete(sql = "UPDATE rental_location SET deleted = true, deleted_at = now() WHERE id = ? and version = ?")
@Where(clause = "deleted = false")
@Getter
@Setter
public class RentalLocation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private RentalCompany company;

    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 120)
    private String city;
    @Column(nullable = false, length = 120)
    private String country;

    @Column(name = "deleted", nullable = false, columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}


