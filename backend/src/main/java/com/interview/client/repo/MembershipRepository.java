package com.interview.client.repo;

import com.interview.client.domain.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    // Atomic increment without optimistic locking
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Membership m set m.points = m.points + :delta where m.id = :id")
    int addPoints(@Param("id") Long id, @Param("delta") int delta);
}
