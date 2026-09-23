package com.caters.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.caters.entity.Caterers;
import com.caters.entity.Users;

@Repository
public interface CaterersRepository extends JpaRepository<Caterers, Long> {

    // Admin side: Eagerly fetch user to prevent LazyInitializationException in templates
    @Override
    @Query("SELECT DISTINCT c FROM Caterers c LEFT JOIN FETCH c.user ORDER BY c.id DESC")
    List<Caterers> findAll();

    // Single caterer lookup with user eagerly loaded for detail view
    @Query("SELECT c FROM Caterers c LEFT JOIN FETCH c.user WHERE c.id = :id")
    Optional<Caterers> findByIdWithUser(@Param("id") Long id);

    // Find caterer profile associated with a specific Users account
    Optional<Caterers> findByUser(Users user);

    // Find caterer profile directly by user ID (foreign key)
    Optional<Caterers> findByUserId(Long userId);

    // List verified caterers to show to customers on the listing page
    List<Caterers> findByIsVerifiedTrue();

    // List unverified caterers for an admin approval dashboard
    List<Caterers> findByIsVerifiedFalse();

    // Search verified caterers by business name containing keywords
    List<Caterers> findByBusinessNameContainingIgnoreCaseAndIsVerifiedTrue(String keyword);

    @Query("SELECT c FROM Caterers c JOIN FETCH c.user WHERE c.user.email = :email")
    Optional<Caterers> findByUserEmail(@Param("email") String email);
}