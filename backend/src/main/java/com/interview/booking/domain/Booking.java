package com.interview.booking.domain;

import com.interview.client.domain.Client;
import com.interview.company.domain.RentalLocation;
import com.interview.fleet.domain.Car;
import com.interview.payment.domain.Payment;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "booking",
        indexes = {
                @Index(name = "ix_booking_client", columnList = "client_id"),
                @Index(name = "ix_booking_car", columnList = "car_id"),
                @Index(name = "ix_booking_pickup_time", columnList = "pickup_time")
        })
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version; // optimistic locking for status/price updates

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.CREATED;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_location_id")
    private RentalLocation pickupLocation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "return_location_id")
    private RentalLocation returnLocation;

    @Column(name = "pickup_time", nullable = false)
    private Instant pickupTime;

    @Column(name = "return_time", nullable = false)
    private Instant returnTime;

    @Column(nullable = false)
    private Integer totalPriceCents;

//    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<BookingAddOn> addOns = new ArrayList<>();

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Payment payment;
}
