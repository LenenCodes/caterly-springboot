package com.caters.service;

import com.caters.entity.Customers;
import com.caters.entity.Users;

import java.util.Optional;

public interface CustomerService {

    Customers getCustomerByUser(Users user);

    Customers getCustomerByEmail(String email);

    Optional<Customers> getCustomerById(Long id);

    Customers updateCustomerProfile(Long customerId, String name, String phone, String address);
}