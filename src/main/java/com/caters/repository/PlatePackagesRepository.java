package com.caters.repository;

import com.caters.entity.Caterers;
import com.caters.entity.PlatePackages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatePackagesRepository extends JpaRepository<PlatePackages, Long> {

    // Fetch all packages for a caterer's management dashboard
    List<PlatePackages> findByCatererOrderByIdDesc(Caterers caterer);

    // Fetch only active packages for customer browsing
    List<PlatePackages> findByCatererAndIsActiveTrue(Caterers caterer);

    // Fetch active packages with bundled default dishes eagerly loaded for customer menu view
    @Query("SELECT DISTINCT p FROM PlatePackages p LEFT JOIN FETCH p.defaultDishes WHERE p.caterer = :caterer AND p.isActive = true")
    List<PlatePackages> findActivePackagesWithDishesByCaterer(@Param("caterer") Caterers caterer);

    // Secure lookup ensuring a caterer can only edit their own package
    Optional<PlatePackages> findByIdAndCaterer(Long id, Caterers caterer);

    // Fetch single package along with default dishes initialized in one query
    @Query("SELECT p FROM PlatePackages p LEFT JOIN FETCH p.defaultDishes WHERE p.id = :id")
    Optional<PlatePackages> findByIdWithDishes(@Param("id") Long id);
}