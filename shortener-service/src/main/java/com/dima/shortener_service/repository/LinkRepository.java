package com.dima.shortener_service.repository;

import com.dima.shortener_service.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    void deleteByShortCode(String shortCode);
}
