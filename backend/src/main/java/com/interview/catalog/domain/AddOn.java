package com.interview.catalog.domain;

import com.interview.common.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "add_on")
public class AddOn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String code; // GPS, CHILD_SEAT, INSURANCE_PLUS

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false)
    private Integer dailyPriceCents;
}

