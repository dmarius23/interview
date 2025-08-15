package com.interview.outboxevent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.booking.service.impl.BookingPaymentOrchestrator;
import com.interview.common.annotation.Loggable;
import com.interview.outboxevent.domain.OutboxEvent;
import com.interview.outboxevent.domain.OutboxEventStatus;
import com.interview.outboxevent.repo.OutboxEventRepository;
import com.interview.payment.dto.PaymentRequestEvent;
import com.interview.payment.dto.PaymentResponseEvent;
import com.interview.payment.service.impl.PaymentSimulatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Loggable
@Slf4j
public class OutboxEventProcessorService {

    private final OutboxEventRepository outboxEventRepository;
    private final PaymentSimulatorService paymentSimulatorService;
    private final BookingPaymentOrchestrator bookingPaymentOrchestrator;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 20000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedDate(
                OutboxEventStatus.PENDING, PageRequest.of(0, 10));

        for (OutboxEvent event : pendingEvents) {
            try {
                processEvent(event);
            } catch (Exception e) {
                handleEventProcessingError(event, e);
            }
        }
    }

    @Async("sagaExecutor")
    public void processEvent(OutboxEvent event) throws JsonProcessingException {
        try {
            event.setStatus(OutboxEventStatus.PROCESSING);
            outboxEventRepository.save(event);

            switch (event.getEventType()) {
                case "PAYMENT_REQUESTED":
                    handlePaymentRequest(event);
                    break;
                case "BOOKING_CONFIRMED":
                case "BOOKING_CANCELED":
                    // These could trigger other downstream processes
                    log.info("Processing event: {}", event.getEventType());
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }

            event.setStatus(OutboxEventStatus.COMPLETED);
            event.setProcessedAt(LocalDateTime.now());
            outboxEventRepository.save(event);

        } catch (Exception e) {
            log.error("Failed to process outbox event: {}", event.getId(), e);
            throw e;
        }
    }

    private void handlePaymentRequest(OutboxEvent event) throws JsonProcessingException {
        PaymentRequestEvent paymentRequest = objectMapper.readValue(
                event.getEventData(), PaymentRequestEvent.class);

        // Simulate async payment processing
        PaymentResponseEvent paymentResponse = paymentSimulatorService.processPayment(paymentRequest);

        // Handle the response in the saga
        bookingPaymentOrchestrator.handlePaymentResponse(paymentResponse);
    }

    private void handleEventProcessingError(OutboxEvent event, Exception e) {
        event.setStatus(OutboxEventStatus.FAILED);
        event.setRetryCount(event.getRetryCount() + 1);
        event.setErrorMessage(e.getMessage());
        outboxEventRepository.save(event);

        log.error("Failed to process outbox event: {} after {} retries",
                event.getId(), event.getRetryCount(), e);
    }

    @Scheduled(fixedDelay = 30000) // Retry failed events every 30 seconds
    @Transactional
    public void retryFailedEvents() {
        List<OutboxEvent> failedEvents = outboxEventRepository.findFailedEventsForRetry(
                3, PageRequest.of(0, 5)); // Max 3 retries, process 5 at a time

        for (OutboxEvent event : failedEvents) {
            log.info("Retrying failed event: {} (attempt {})", event.getId(), event.getRetryCount() + 1);
            event.setStatus(OutboxEventStatus.PENDING);
            event.setErrorMessage(null);
            outboxEventRepository.save(event);
        }
    }
}
