package com.caters.repository;

import com.caters.entity.Caterers;
import com.caters.entity.Customers;
import com.caters.entity.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewsRepository extends JpaRepository<Reviews, Long> {

    List<Reviews> findByCatererOrderByCreatedAtDesc(Caterers caterer);

    Optional<Reviews> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    // Calculate aggregate score (e.g. 4.75)
    @Query("SELECT AVG(r.rating) FROM Reviews r WHERE r.caterer.id = :catererId")
    Double getAverageRatingForCaterer(@Param("catererId") Long catererId);

    // Total review count for badges
    long countByCaterer(Caterers caterer);
    List<Reviews> findByCustomer(Customers customer);
}