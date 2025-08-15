package com.interview.booking.domain;

import com.interview.catalog.domain.AddOn;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Getter
@Setter
@Entity
@Table(name = "booking_addon")
public class BookingAddOn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_id")
    private AddOn addOn;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false)
    private Integer dailyPriceCentsSnapshot;
}

