package com.hardwarestore.hardwarestoremanagement.repository;

import com.hardwarestore.hardwarestoremanagement.entity.CustomerRequest;
import com.hardwarestore.hardwarestoremanagement.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRequestRepository extends JpaRepository<CustomerRequest, Long> {

    Optional<CustomerRequest> findByRequestIdIgnoreCase(String requestId);

    List<CustomerRequest> findAllByOrderByCreatedAtDesc();

    List<CustomerRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
}