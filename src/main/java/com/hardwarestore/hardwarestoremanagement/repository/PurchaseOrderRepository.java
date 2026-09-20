package com.hardwarestore.hardwarestoremanagement.repository;

import com.hardwarestore.hardwarestoremanagement.entity.OrderStatus;
import com.hardwarestore.hardwarestoremanagement.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    List<PurchaseOrder> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);

    List<PurchaseOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<PurchaseOrder> findByStatusInOrderByCreatedAtDesc(List<OrderStatus> statuses);
}