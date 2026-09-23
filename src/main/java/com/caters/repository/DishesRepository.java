package com.caters.repository;

import com.caters.entity.Caterers;
import com.caters.entity.Dishes;
import com.caters.enums.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishesRepository extends JpaRepository<Dishes, Long> {

    // Fetch all dishes belonging to a specific caterer (newest first)
    List<Dishes> findByCatererOrderByIdDesc(Caterers caterer);

    // Fetch only active/available dishes for customer viewing & swapping
    List<Dishes> findByCatererAndIsAvailableTrue(Caterers caterer);

    // Filter available dishes by course category (e.g., all available STARTER items)
    List<Dishes> findByCatererAndCourseTypeAndIsAvailableTrue(Caterers caterer, CourseType courseType);

    // Secure lookup ensuring a caterer can only modify their own dish
    Optional<Dishes> findByIdAndCaterer(Long id, Caterers caterer);
}