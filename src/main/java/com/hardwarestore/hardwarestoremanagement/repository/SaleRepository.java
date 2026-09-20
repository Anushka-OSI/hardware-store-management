package com.hardwarestore.hardwarestoremanagement.repository;

import com.hardwarestore.hardwarestoremanagement.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDateTime start, LocalDateTime end);

    Long countBySaleDateBetween(LocalDateTime start, LocalDateTime end);
}