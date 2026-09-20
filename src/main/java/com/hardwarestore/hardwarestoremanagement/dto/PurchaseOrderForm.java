package com.hardwarestore.hardwarestoremanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderForm(
        Long supplierId,
        LocalDate orderDate,
        LocalDate expectedDeliveryDate,
        String notes,
        List<Long> productIds,
        List<Integer> quantities,
        List<BigDecimal> unitCosts
) {}