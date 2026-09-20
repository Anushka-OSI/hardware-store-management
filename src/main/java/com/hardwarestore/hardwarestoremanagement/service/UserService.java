package com.hardwarestore.hardwarestoremanagement.service;

import com.hardwarestore.hardwarestoremanagement.dto.StaffForm;
import com.hardwarestore.hardwarestoremanagement.dto.SupplierForm;
import com.hardwarestore.hardwarestoremanagement.entity.Role;
import com.hardwarestore.hardwarestoremanagement.entity.User;
import com.hardwarestore.hardwarestoremanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> allUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<User> allActiveSuppliers() {
        return userRepository.findByRole(Role.SUPPLIER).stream().filter(User::isActive).toList();
    }

    public List<User> suppliers() {
        return userRepository.findByRoleOrderByCreatedAtDesc(Role.SUPPLIER);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createStaff(StaffForm form) {
        if (userRepository.existsByUsername(form.username())) {
            throw new IllegalArgumentException("Username already exists: " + form.username());
        }
        User u = new User();
        u.setUsername(form.username());
        u.setFullName(form.fullName());
        u.setEmail(form.email());
        u.setPhone(form.phone());
        u.setPassword(passwordEncoder.encode(form.password()));
        u.setRole(form.role());
        u.setActive(form.active());
        return userRepository.save(u);
    }

    @Transactional
    public User updateStaff(StaffForm form) {
        User u = userRepository.findById(form.id()).orElseThrow();
        u.setFullName(form.fullName());
        u.setEmail(form.email());
        u.setPhone(form.phone());
        if (form.password() != null && !form.password().isBlank()) {
            u.setPassword(passwordEncoder.encode(form.password()));
        }
        if (form.role() != null) {
            u.setRole(form.role());
        }
        u.setActive(form.active());
        return userRepository.save(u);
    }

    @Transactional
    public User createSupplier(SupplierForm form) {
        if (userRepository.existsByUsername(form.username())) {
            throw new IllegalArgumentException("Username already exists: " + form.username());
        }
        User u = new User();
        u.setUsername(form.username());
        u.setFullName(form.fullName() == null || form.fullName().isBlank() ? form.businessName() : form.fullName());
        u.setEmail(form.email());
        u.setPhone(form.phone());
        u.setPassword(passwordEncoder.encode(form.password()));
        u.setRole(Role.SUPPLIER);
        u.setBusinessName(form.businessName());
        u.setContactPerson(form.contactPerson());
        u.setAddress(form.address());
        u.setActive(true);
        return userRepository.save(u);
    }

    @Transactional
    public User updateSupplier(SupplierForm form) {
        User u = userRepository.findById(form.id()).orElseThrow();
        u.setFullName(form.fullName() == null || form.fullName().isBlank() ? form.businessName() : form.fullName());
        u.setEmail(form.email());
        u.setPhone(form.phone());
        if (form.password() != null && !form.password().isBlank()) {
            u.setPassword(passwordEncoder.encode(form.password()));
        }
        u.setBusinessName(form.businessName());
        u.setContactPerson(form.contactPerson());
        u.setAddress(form.address());
        u.setActive(form.active());
        return userRepository.save(u);
    }

    @Transactional
    public User setActive(Long userId, boolean active) {
        User u = userRepository.findById(userId).orElseThrow();
        u.setActive(active);
        return userRepository.save(u);
    }
}