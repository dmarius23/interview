package com.interview.outboxevent.repo;

import com.interview.outboxevent.domain.OutboxEvent;
import com.interview.outboxevent.domain.OutboxEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdDate ASC")
    List<OutboxEvent> findByStatusOrderByCreatedDate(@Param("status") OutboxEventStatus status, Pageable pageable);

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'FAILED' AND o.retryCount < :maxRetries ORDER BY o.createdDate ASC")
    List<OutboxEvent> findFailedEventsForRetry(@Param("maxRetries") int maxRetries, Pageable pageable);
}
