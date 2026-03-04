package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.customer.CustomerCreateRequest;
import com.dibimbing.inventory_sales_api.dto.customer.CustomerResponse;
import com.dibimbing.inventory_sales_api.entity.Customer;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerResponse create(CustomerCreateRequest req) {
        Customer c = Customer.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .build();
        Customer saved = customerRepository.save(c);
        return toResponse(saved);
    }

    public CustomerResponse get(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
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