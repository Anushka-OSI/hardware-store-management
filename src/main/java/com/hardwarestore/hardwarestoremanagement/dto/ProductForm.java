package com.hardwarestore.hardwarestoremanagement.dto;

import java.math.BigDecimal;

public record ProductForm(
        Long id,
        String sku,
        String name,
        String brand,
        String category,
        String description,
        BigDecimal price,
        int quantityInStock,
        int minStockLevel,
        Long supplierId
) {}