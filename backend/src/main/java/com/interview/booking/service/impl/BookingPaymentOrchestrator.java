package com.interview.booking.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.booking.domain.Booking;
import com.interview.booking.domain.BookingStatus;
import com.interview.booking.dto.BookingCanceledEvent;
import com.interview.booking.dto.BookingConfirmedEvent;
import com.interview.booking.repo.BookingRepository;
import com.interview.common.annotation.Loggable;
import com.interview.common.domain.EntityNotFound;
import com.interview.outboxevent.domain.OutboxEvent;
import com.interview.outboxevent.repo.OutboxEventRepository;
import com.interview.payment.dto.PaymentRequestEvent;
import com.interview.payment.dto.PaymentResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@Loggable(logParams = true, logResult = false)
@Slf4j
@RequiredArgsConstructor
public class BookingPaymentOrchestrator {
    private final BookingRepository bookingRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;


    /**
     * Create booking with PENDING status and publish payment request event
     */
    public void startBookingSaga(Booking booking) {
        // Set booking status to PENDING
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        // Create payment request event
        PaymentRequestEvent paymentEvent = new PaymentRequestEvent(
                booking.getId(),
                booking.getClient().getId(),
                booking.getTotalPriceCents(),
                "USD",
                LocalDateTime.now()
        );

        // Publish to outbox
        publishEvent(
                booking.getId().toString(),
                "Booking",
                "PAYMENT_REQUESTED",
                paymentEvent
        );

        log.info("Booking saga started for booking: {}", booking.getId());
    }

    public void handlePaymentResponse(PaymentResponseEvent paymentResponse) {
        Booking booking = bookingRepository.findById(paymentResponse.getBookingId())
                .orElseThrow(() -> new EntityNotFound("Booking not found: " + paymentResponse.getBookingId()));

        if (paymentResponse.isSuccess()) {
            // Payment successful - confirm booking
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Publish booking confirmed event
            publishEvent(
                    booking.getId().toString(),
                    "Booking",
                    "BOOKING_CONFIRMED",
                    new BookingConfirmedEvent(booking.getId(), paymentResponse.getTransactionId())
            );

            log.info("Booking confirmed: {} with transaction: {}",
                    booking.getId(), paymentResponse.getTransactionId());
        } else {
            // Payment failed - cancel booking and release car
            booking.setStatus(BookingStatus.CANCELED);
            //booking.getCar().setStatus(CarStatus.AVAILABLE);
            bookingRepository.save(booking);

            // Publish booking canceled event
            publishEvent(
                    booking.getId().toString(),
                    "Booking",
                    "BOOKING_CANCELED",
                    new BookingCanceledEvent(booking.getId(), paymentResponse.getErrorMessage())
            );

            log.warn("Booking canceled due to payment failure: {} - {}",
                    booking.getId(), paymentResponse.getErrorMessage());
        }
    }

    private void publishEvent(String aggregateId, String aggregateType, String eventType, Object eventData) {
        try {
            String eventDataJson = objectMapper.writeValueAsString(eventData);
            OutboxEvent outboxEvent = new OutboxEvent(aggregateId, aggregateType, eventType, eventDataJson);
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}

