package com.hardwarestore.hardwarestoremanagement.dto;

import com.hardwarestore.hardwarestoremanagement.entity.Role;

public record StaffForm(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        String password,
        Role role,
        boolean active
) {}