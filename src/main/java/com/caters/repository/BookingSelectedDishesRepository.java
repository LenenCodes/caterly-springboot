package com.caters.repository;

import com.caters.entity.BookingSelectedDishes;
import com.caters.entity.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSelectedDishesRepository extends JpaRepository<BookingSelectedDishes, Long> {

    List<BookingSelectedDishes> findByBooking(Bookings booking);
}