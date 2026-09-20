package com.hardwarestore.hardwarestoremanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String brand;

    private String category;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false)
    private int quantityInStock = 0;

    @Column(nullable = false)
    private int minStockLevel = 0;

    private String photoPath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id")
    private User supplier;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ---- Automatic availability status ----
    public boolean isAvailable() {
        return quantityInStock > 0;
    }

    public boolean isLowStock() {
        return quantityInStock > 0 && quantityInStock <= minStockLevel;
    }

    public boolean isOutOfStock() {
        return quantityInStock <= 0;
    }

    public String getStatusLabel() {
        if (isOutOfStock()) return "OUT OF STOCK";
        if (isLowStock()) return "LOW STOCK";
        return "AVAILABLE";
    }
}