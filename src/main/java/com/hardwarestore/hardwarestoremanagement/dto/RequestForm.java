package com.hardwarestore.hardwarestoremanagement.dto;

public record RequestForm(
        String customerName,
        String customerPhone,
        String message
) {}