package com.dima.analytics_service.repository;

import com.dima.analytics_service.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    @Query("SELECT MAX(c.clickedAt) FROM ClickEvent c WHERE c.shortCode = :shortCode")
    Instant findLastClickedAt(String shortCode);

    @Query("SELECT COUNT(c) FROM ClickEvent c WHERE c.shortCode = :shortCode")
    Long countByShortCode(String shortCode);
}
