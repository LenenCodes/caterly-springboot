package com.caters.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caters.dto.BookingRequestDto;
import com.caters.entity.BookingSelectedDishes;
import com.caters.entity.Bookings;
import com.caters.entity.Caterers;
import com.caters.entity.Customers;
import com.caters.entity.Dishes;
import com.caters.entity.PlatePackages;
import com.caters.enums.BookingStatus;
import com.caters.repository.BookingsRepository;
import com.caters.repository.DishesRepository;
import com.caters.repository.PlatePackagesRepository;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingsRepository bookingsRepository;
    private final PlatePackagesRepository platePackagesRepository;
    private final DishesRepository dishesRepository;

    public BookingServiceImpl(BookingsRepository bookingsRepository,
                              PlatePackagesRepository platePackagesRepository,
                              DishesRepository dishesRepository) {
        this.bookingsRepository = bookingsRepository;
        this.platePackagesRepository = platePackagesRepository;
        this.dishesRepository = dishesRepository;
    }

    @Override
    @Transactional
    public Bookings createBooking(Customers customer, BookingRequestDto dto) {
        // 1. Fetch plate package with default dishes eagerly loaded
        PlatePackages platePackage = platePackagesRepository.findByIdWithDishes(dto.getPackageId())
                .orElseGet(() -> platePackagesRepository.findById(dto.getPackageId())
                        .orElseThrow(() -> new RuntimeException("Selected plate package not found")));

        if (!platePackage.isActive()) {
            throw new IllegalStateException("This plate package is currently inactive.");
        }

        if (dto.getGuestPlateCount() < platePackage.getMinPlates()) {
            throw new IllegalArgumentException("Minimum order for this package is " + platePackage.getMinPlates() + " plates.");
        }

        Caterers caterer = platePackage.getCaterer();

        // 2. Validate Event Date Constraints
        if (dto.getEventDate() == null || !dto.getEventDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Event date must be scheduled at least one day in advance.");
        }

        // 3. Concurrency Guard: Check if Caterer has an active/locked booking on this date
        List<BookingStatus> lockedStatuses = List.of(BookingStatus.ACCEPTED, BookingStatus.CONFIRMED);
        boolean isDateUnavailable = bookingsRepository.existsByCatererAndEventDateAndStatusIn(
                caterer,
                dto.getEventDate(),
                lockedStatuses
        );

        if (isDateUnavailable) {
            throw new IllegalStateException("This caterer is already booked on " + dto.getEventDate() + ". Please choose another date.");
        }

        // 4. Fetch and validate selected dishes
        if (dto.getSelectedDishIds() == null || dto.getSelectedDishIds().isEmpty()) {
            throw new IllegalArgumentException("At least one dish must be included in your plate.");
        }

        List<Dishes> selectedDishes = dishesRepository.findAllById(dto.getSelectedDishIds());
        if (selectedDishes.isEmpty()) {
            throw new IllegalArgumentException("Selected dishes could not be found.");
        }

        // Extract IDs of dishes that are bundled as default items in this package
        Set<Long> defaultDishIds = platePackage.getDefaultDishes() != null
                ? platePackage.getDefaultDishes().stream().map(Dishes::getId).collect(Collectors.toSet())
                : Set.of();

        Set<Long> selectedDishIdSet = selectedDishes.stream()
                .map(Dishes::getId)
                .collect(Collectors.toSet());

        // 5a. Compute add-ons: selected dishes NOT in default package
        BigDecimal extraCostsPerPlate = BigDecimal.ZERO;
        for (Dishes dish : selectedDishes) {
            if (!defaultDishIds.contains(dish.getId())) {
                BigDecimal effectiveCost = (dish.getExtraCost() != null && dish.getExtraCost().compareTo(BigDecimal.ZERO) > 0)
                        ? dish.getExtraCost()
                        : (dish.getPrice() != null ? dish.getPrice() : BigDecimal.ZERO);

                extraCostsPerPlate = extraCostsPerPlate.add(effectiveCost);
            }
        }

        // 5b. Compute package deduction: package default dishes that the customer excluded (unchecked)
        BigDecimal packageDiscountPerPlate = BigDecimal.ZERO;
        if (platePackage.getDefaultDishes() != null) {
            for (Dishes defaultDish : platePackage.getDefaultDishes()) {
                if (!selectedDishIdSet.contains(defaultDish.getId())) {
                    BigDecimal deduction = (defaultDish.getExtraCost() != null && defaultDish.getExtraCost().compareTo(BigDecimal.ZERO) > 0)
                            ? defaultDish.getExtraCost()
                            : (defaultDish.getPrice() != null ? defaultDish.getPrice() : BigDecimal.ZERO);

                    packageDiscountPerPlate = packageDiscountPerPlate.add(deduction);
                }
            }
        }

        // Net price per plate: (Base Price - Excluded Discounts) + Add-ons
        BigDecimal netBase = platePackage.getBasePricePerPlate().subtract(packageDiscountPerPlate);
        if (netBase.compareTo(BigDecimal.ZERO) < 0) {
            netBase = BigDecimal.ZERO;
        }

        BigDecimal finalPricePerPlate = netBase.add(extraCostsPerPlate);

        // 6. Calculate grand total: (finalPricePerPlate * guestPlateCount)
        BigDecimal totalAmount = finalPricePerPlate.multiply(BigDecimal.valueOf(dto.getGuestPlateCount()));

        // 7. Initialize Booking entity
        Bookings booking = new Bookings(
                customer,
                caterer,
                platePackage,
                dto.getGuestPlateCount(),
                finalPricePerPlate,
                totalAmount,
                dto.getEventDate(),
                dto.getEventTime(),
                dto.getVenueAddress(),
                dto.getSpecialNotes()
        );

        // 8. Snapshot selected dishes as line-items with their actual snapshot charge
        for (Dishes dish : selectedDishes) {
            boolean isPackageDefault = defaultDishIds.contains(dish.getId());
            BigDecimal itemCharge = BigDecimal.ZERO;

            if (!isPackageDefault) {
                itemCharge = (dish.getExtraCost() != null && dish.getExtraCost().compareTo(BigDecimal.ZERO) > 0)
                        ? dish.getExtraCost()
                        : (dish.getPrice() != null ? dish.getPrice() : BigDecimal.ZERO);
            }

            BookingSelectedDishes lineItem = new BookingSelectedDishes(
                    booking,
                    dish,
                    dish.getName(),
                    dish.getCourseType().name(),
                    itemCharge
            );
            booking.addSelectedDish(lineItem);
        }

        return bookingsRepository.save(booking);
    }

    @Override
    public List<Bookings> getBookingsByCustomer(Customers customer) {
        return bookingsRepository.findByCustomerWithDetails(customer);
    }

    @Override
    public List<Bookings> getBookingsByCaterer(Caterers caterer) {
        return bookingsRepository.findByCatererWithDetails(caterer);
    }

    @Override
    public Bookings getBookingDetailsById(Long bookingId) {
        return bookingsRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));
    }

    @Override
    @Transactional
    public void updateBookingStatus(Long bookingId, BookingStatus newStatus, Caterers caterer) {
        Bookings booking = bookingsRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        if (!booking.getCaterer().getId().equals(caterer.getId())) {
            throw new SecurityException("Unauthorized action on this booking.");
        }

        // State Transition Guard: Cannot mark as COMPLETED if final payment is unpaid
        if (newStatus == BookingStatus.COMPLETED && !booking.isFullyPaid()) {
            throw new IllegalStateException("Cannot complete booking #" + bookingId + ". Outstanding balance must be settled first.");
        }

        booking.setStatus(newStatus);
        bookingsRepository.save(booking);
    }

    @Override
    @Transactional
    public void cancelBookingByCustomer(Long bookingId, Customers customer, String reason) {
        Bookings booking = bookingsRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("Unauthorized action on this booking.");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Completed bookings cannot be cancelled.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REJECTED) {
            throw new IllegalStateException("This booking is already closed.");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        String cancellationLog = "[Customer Cancelled: " + (reason != null && !reason.isBlank() ? reason.trim() : "No reason provided") + "]";
        if (booking.getSpecialNotes() != null && !booking.getSpecialNotes().isBlank()) {
            booking.setSpecialNotes(booking.getSpecialNotes() + " | " + cancellationLog);
        } else {
            booking.setSpecialNotes(cancellationLog);
        }

        bookingsRepository.save(booking);
    }
}