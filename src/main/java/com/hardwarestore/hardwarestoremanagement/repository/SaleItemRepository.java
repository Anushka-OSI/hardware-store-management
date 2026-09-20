package com.hardwarestore.hardwarestoremanagement.repository;

import com.hardwarestore.hardwarestoremanagement.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    List<SaleItem> findBySaleSaleDateBetween(LocalDateTime start, LocalDateTime end);

    Long countBySaleSaleDateBetween(LocalDateTime start, LocalDateTime end);
}