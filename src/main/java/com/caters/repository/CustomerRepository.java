package com.caters.repository;

import com.caters.entity.Customers;
import com.caters.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customers, Long> {

    // Find the customer profile associated with a specific Users account
    Optional<Customers> findByUser(Users user);

    // Find customer profile directly by user ID (foreign key)
    Optional<Customers> findByUserId(Long userId);

    // Optional: Search customers by phone number
    Optional<Customers> findByPhone(String phone);
    @Query("SELECT c FROM Customers c JOIN FETCH c.user WHERE c.user.email = :email")
    Optional<Customers> findByUserEmail(@Param("email") String email);
}