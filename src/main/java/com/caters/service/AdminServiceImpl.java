package com.caters.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caters.entity.Bookings;
import com.caters.entity.Caterers;
import com.caters.entity.Customers;
import com.caters.repository.BookingsRepository;
import com.caters.repository.CaterersRepository;
import com.caters.repository.CustomerRepository;

@Service
public class AdminServiceImpl implements AdminService {

    private final CustomerRepository customerRepository;
    private final CaterersRepository caterersRepository;
    private final BookingsRepository bookingsRepository;

    public AdminServiceImpl(CustomerRepository customerRepository,
                            CaterersRepository caterersRepository,
                            BookingsRepository bookingsRepository) {
        this.customerRepository = customerRepository;
        this.caterersRepository = caterersRepository;
        this.bookingsRepository = bookingsRepository;
    }

    @Override
    public List<Caterers> getPendingCaterers() {
        return caterersRepository.findByIsVerifiedFalse();
    }

    @Override
    public List<Caterers> getVerifiedCaterers() {
        return caterersRepository.findByIsVerifiedTrue();
    }

    @Override
    public long getTotalCustomerCount() {
        return customerRepository.count();
    }

    @Override
    @Transactional
    public void verifyCaterer(Long id) {
        Caterers caterer = getCatererById(id);
        caterer.setVerified(true);
        caterersRepository.save(caterer);
    }

    @Override
    @Transactional
    public void revokeCaterer(Long id) {
        Caterers caterer = getCatererById(id);
        caterer.setVerified(false);
        caterersRepository.save(caterer);
    }

    // Customer Operations
    @Override
    public List<Customers> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customers getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + id));
    }

    @Override
    @Transactional
    public void updateCustomer(Long id, String name, String phone, String address) {
        Customers customer = getCustomerById(id);
        customer.setName(name);
        customer.setPhone(phone);
        customer.setAddress(address);
        customerRepository.save(customer);
    }

    @Override
    @Transactional
    public void toggleCustomerStatus(Long id) {
        Customers customer = getCustomerById(id);
        if (customer.getUser() != null) {
            customer.getUser().setEnabled(!customer.getUser().isEnabled());
        }
        customerRepository.save(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customers customer = getCustomerById(id);
        List<Bookings> orders = bookingsRepository.findByCustomerWithDetails(customer);
        if (!orders.isEmpty()) {
            throw new IllegalStateException("Cannot delete customer with historical bookings. Suspend the account instead.");
        }
        customerRepository.delete(customer);
    }

    // Caterer Operations
    @Override
    public List<Caterers> getAllCaterers() {
        return caterersRepository.findAll();
    }

    @Override
    public Caterers getCatererById(Long id) {
        return caterersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Caterer not found with ID: " + id));
    }

    @Override
    @Transactional
    public void updateCaterer(Long id, String businessName, String phone, String address, String description) {
        Caterers caterer = getCatererById(id);
        caterer.setBusinessName(businessName);
        caterer.setPhone(phone);
        caterer.setAddress(address);
        caterer.setDescription(description);
        caterersRepository.save(caterer);
    }

    @Override
    @Transactional
    public void toggleCatererAccountStatus(Long id) {
        Caterers caterer = getCatererById(id);
        if (caterer.getUser() != null) {
            caterer.getUser().setEnabled(!caterer.getUser().isEnabled());
        }
        caterersRepository.save(caterer);
    }

    @Override
    @Transactional
    public void deleteCaterer(Long id) {
        Caterers caterer = getCatererById(id);
        List<Bookings> bookings = bookingsRepository.findByCatererWithDetails(caterer);
        if (!bookings.isEmpty()) {
            throw new IllegalStateException("Cannot delete caterer with active or historical event contracts. Revoke verification or suspend instead.");
        }
        caterersRepository.delete(caterer);
    }

    // Bookings Ledger
    @Override
    public List<Bookings> getAllBookings() {
        return bookingsRepository.findAll();
    }
}