package com.hardwarestore.hardwarestoremanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantityOrdered;

    @Column(nullable = false)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(nullable = false)
    private int quantityReceived = 0;

    @Column(length = 500)
    private String notes;

    public BigDecimal getLineTotal() {
        return unitCost.multiply(BigDecimal.valueOf(quantityOrdered));
    }
}