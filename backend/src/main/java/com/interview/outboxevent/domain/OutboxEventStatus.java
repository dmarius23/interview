package com.interview.outboxevent.domain;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
