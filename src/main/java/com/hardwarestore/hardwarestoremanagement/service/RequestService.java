package com.hardwarestore.hardwarestoremanagement.service;

import com.hardwarestore.hardwarestoremanagement.dto.RequestForm;
import com.hardwarestore.hardwarestoremanagement.entity.CustomerRequest;
import com.hardwarestore.hardwarestoremanagement.entity.RequestStatus;
import com.hardwarestore.hardwarestoremanagement.repository.CustomerRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RequestService {

    private final CustomerRequestRepository requestRepository;

    public RequestService(CustomerRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Transactional
    public CustomerRequest submit(RequestForm form) {
        CustomerRequest r = new CustomerRequest();
        r.setRequestId(generateRequestId());
        r.setCustomerName(form.customerName().trim());
        r.setCustomerPhone(form.customerPhone().trim());
        r.setMessage(form.message().trim());
        r.setStatus(RequestStatus.PENDING);
        return requestRepository.save(r);
    }

    private String generateRequestId() {
        return "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Optional<CustomerRequest> findByRequestId(String requestId) {
        return requestRepository.findByRequestIdIgnoreCase(requestId);
    }

    public Optional<CustomerRequest> findById(Long id) {
        return requestRepository.findById(id);
    }

    public List<CustomerRequest> allRequests() {
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<CustomerRequest> byStatus(RequestStatus status) {
        return requestRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional
    public CustomerRequest updateStatus(Long id, RequestStatus status, String response) {
        CustomerRequest r = requestRepository.findById(id).orElseThrow();
        r.setStatus(status);
        if (response != null && !response.isBlank()) {
            r.setAdminResponse(response.trim());
        }
        return requestRepository.save(r);
    }
}