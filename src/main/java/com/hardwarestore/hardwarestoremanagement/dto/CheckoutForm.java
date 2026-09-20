package com.hardwarestore.hardwarestoremanagement.dto;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutForm(
        List<Long> productIds,
        List<Integer> quantities,
        BigDecimal discount,
        PaymentMethodDto paymentMethod
) {
    public enum PaymentMethodDto { CASH, CARD, DIGITAL }
}