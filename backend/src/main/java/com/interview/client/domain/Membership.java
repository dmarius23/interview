package com.interview.client.domain;

import com.interview.common.domain.BaseEntity;
import com.interview.company.domain.RentalCompany;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "membership",
        uniqueConstraints = @UniqueConstraint(name = "uk_membership_client_company", columnNames = {"client_id", "company_id"}))
public class Membership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NOTE: No @Version here to demonstrate atomic-update alternative for points

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private RentalCompany company;

    @Column(nullable = false, length = 20)
    private String tier; // BRONZE/SILVER/GOLD

    @Column(nullable = false)
    private Integer points = 0;

    @Column(nullable = false)
    private Instant joinedAt = Instant.now();
}
