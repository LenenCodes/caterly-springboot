package com.caters.service;

import com.caters.dto.ReviewDto;
import com.caters.entity.Bookings;
import com.caters.entity.Caterers;
import com.caters.entity.Customers;
import com.caters.entity.Reviews;
import com.caters.enums.BookingStatus;
import com.caters.repository.BookingsRepository;
import com.caters.repository.ReviewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewsRepository reviewsRepository;
    private final BookingsRepository bookingsRepository;

    public ReviewServiceImpl(ReviewsRepository reviewsRepository, BookingsRepository bookingsRepository) {
        this.reviewsRepository = reviewsRepository;
        this.bookingsRepository = bookingsRepository;
    }

    @Override
    @Transactional
    public Reviews submitReview(Customers customer, ReviewDto dto) {
        Bookings booking = bookingsRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Validation Rules:
        // 1. Customer must own this booking
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("Unauthorized review submission.");
        }

        // 2. Order status must be COMPLETED
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Reviews can only be submitted for completed catering events.");
        }

        // 3. Exactly 1 review per booking
        if (reviewsRepository.existsByBookingId(booking.getId())) {
            throw new IllegalStateException("You have already submitted a review for this booking.");
        }

        Reviews review = new Reviews(
                customer,
                booking.getCaterer(),
                booking,
                dto.getRating(),
                dto.getComment()
        );

        return reviewsRepository.save(review);
    }

    @Override
    public List<Reviews> getReviewsForCaterer(Caterers caterer) {
        return reviewsRepository.findByCatererOrderByCreatedAtDesc(caterer);
    }

    @Override
    public Optional<Reviews> getReviewByBookingId(Long bookingId) {
        return reviewsRepository.findByBookingId(bookingId);
    }
    @Override
    public List<Reviews> getReviewsByCustomer(Customers customer) {
        return reviewsRepository.findByCustomer(customer);
    }
    @Override
    public Double getAverageRatingForCaterer(Long catererId) {
        Double avg = reviewsRepository.getAverageRatingForCaterer(catererId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }
}