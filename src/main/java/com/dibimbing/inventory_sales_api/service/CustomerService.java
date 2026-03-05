package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.customer.CustomerCreateRequest;
import com.dibimbing.inventory_sales_api.dto.customer.CustomerResponse;
import com.dibimbing.inventory_sales_api.entity.Customer;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

    private final CustomerRepository customerRepository;

    public CustomerResponse create(CustomerCreateRequest req) {
        log.info("CustomerService.create called name={} email={}", req.getName(), req.getEmail());

        Customer c = Customer.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .build();

        Customer saved = customerRepository.save(c);

        log.info("Customer created customerId={} name={}", saved.getId(), saved.getName());
        AUDIT.info("CUSTOMER_CREATE customerId=%d name=%s email=%s".formatted(saved.getId(), saved.getName(), saved.getEmail()));

        return toResponse(saved);
    }

    public CustomerResponse get(Long id) {
        log.debug("CustomerService.get called id={}", id);

        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found id={}", id);
                    return new NotFoundException("Customer not found");
                });

        return toResponse(c);
    }

    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .address(c.getAddress())
                .build();
    }
}