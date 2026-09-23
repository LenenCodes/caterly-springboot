package com.caters.service;

import java.util.List;
import com.caters.entity.Bookings;
import com.caters.entity.Caterers;
import com.caters.entity.Customers;

public interface AdminService {

    // Metrics & Lists for Dashboard
    List<Caterers> getPendingCaterers();
    List<Caterers> getVerifiedCaterers();
    long getTotalCustomerCount();

    // Specific Actions expected by Dashboard
    void verifyCaterer(Long id);
    void revokeCaterer(Long id);

    // Customer Operations
    List<Customers> getAllCustomers();
    Customers getCustomerById(Long id);
    void updateCustomer(Long id, String name, String phone, String address);
    void toggleCustomerStatus(Long id);
    void deleteCustomer(Long id);

    // Caterer Operations
    List<Caterers> getAllCaterers();
    Caterers getCatererById(Long id);
    void updateCaterer(Long id, String businessName, String phone, String address, String description);
    void toggleCatererAccountStatus(Long id);
    void deleteCaterer(Long id);

    // Global Ledger
    List<Bookings> getAllBookings();
}