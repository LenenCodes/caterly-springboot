package com.caters.service;

import com.caters.entity.Customers;
import com.caters.entity.Users;
import com.caters.repository.CustomerRepository;
import com.caters.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UsersRepository usersRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, UsersRepository usersRepository) {
        this.customerRepository = customerRepository;
        this.usersRepository = usersRepository;
    }

    @Override
    public Customers getCustomerByUser(Users user) {
        return customerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer profile not found for user: " + user.getEmail()));
    }

    @Override
    public Customers getCustomerByEmail(String email) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User account not found with email: " + email));
        return getCustomerByUser(user);
    }

    @Override
    public Optional<Customers> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    @Transactional
    public Customers updateCustomerProfile(Long customerId, String name, String phone, String address) {
        Customers customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        customer.setName(name);
        customer.setPhone(phone);
        customer.setAddress(address);

        return customerRepository.save(customer);
    }
}