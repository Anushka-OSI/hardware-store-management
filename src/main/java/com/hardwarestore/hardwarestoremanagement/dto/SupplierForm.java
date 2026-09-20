package com.hardwarestore.hardwarestoremanagement.dto;

public record SupplierForm(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        String password,
        String businessName,
        String contactPerson,
        String address,
        boolean active
) {}