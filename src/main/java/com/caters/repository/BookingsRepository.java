package com.caters.repository;

import com.caters.entity.Bookings;
import com.caters.entity.Caterers;
import com.caters.entity.Customers;
import com.caters.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingsRepository extends JpaRepository<Bookings, Long> {

    // Admin Ledger: Eagerly fetch customer and caterer to avoid N+1 and LazyInitializationException
    @Override
    @Query("SELECT DISTINCT b FROM Bookings b " +
           "LEFT JOIN FETCH b.customer " +
           "LEFT JOIN FETCH b.caterer " +
           "ORDER BY b.createdAt DESC")
    List<Bookings> findAll();

    // Customer side: List booking history with package and dishes eagerly loaded
    @Query("SELECT DISTINCT b FROM Bookings b " +
           "LEFT JOIN FETCH b.caterer " +
           "LEFT JOIN FETCH b.platePackage " +
           "LEFT JOIN FETCH b.selectedDishes " +
           "WHERE b.customer = :customer " +
           "ORDER BY b.createdAt DESC")
    List<Bookings> findByCustomerWithDetails(@Param("customer") Customers customer);

    // Caterer side: List all incoming bookings with customer, package, and dishes
    @Query("SELECT DISTINCT b FROM Bookings b " +
           "LEFT JOIN FETCH b.customer " +
           "LEFT JOIN FETCH b.platePackage " +
           "LEFT JOIN FETCH b.selectedDishes " +
           "WHERE b.caterer = :caterer " +
           "ORDER BY b.createdAt DESC")
    List<Bookings> findByCatererWithDetails(@Param("caterer") Caterers caterer);

    // Caterer side: Filter incoming bookings by status
    List<Bookings> findByCatererAndStatusOrderByEventDateAsc(Caterers caterer, BookingStatus status);

    // Eagerly fetch single booking details
    @Query("SELECT b FROM Bookings b " +
           "LEFT JOIN FETCH b.selectedDishes " +
           "JOIN FETCH b.customer " +
           "JOIN FETCH b.caterer " +
           "JOIN FETCH b.platePackage " +
           "WHERE b.id = :id")
    Optional<Bookings> findByIdWithDetails(@Param("id") Long id);

    // Conflict Guard: Check if caterer already has an active/locked booking on this event date
    @Query("SELECT COUNT(b) > 0 FROM Bookings b " +
           "WHERE b.caterer = :caterer " +
           "AND b.eventDate = :eventDate " +
           "AND b.status IN :statuses")
    boolean existsByCatererAndEventDateAndStatusIn(
            @Param("caterer") Caterers caterer,
            @Param("eventDate") LocalDate eventDate,
            @Param("statuses") Collection<BookingStatus> statuses);

    // Fetch all locked event dates for a caterer to feed into UI calendar lockout
    @Query("SELECT DISTINCT b.eventDate FROM Bookings b " +
           "WHERE b.caterer = :caterer " +
           "AND b.status IN :statuses")
    List<LocalDate> findBookedDatesByCatererAndStatusIn(
            @Param("caterer") Caterers caterer,
            @Param("statuses") Collection<BookingStatus> statuses);
}