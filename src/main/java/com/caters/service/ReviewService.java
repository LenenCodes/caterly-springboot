package com.caters.service;

import com.caters.dto.ReviewDto;
import com.caters.entity.Caterers;
import com.caters.entity.Customers;
import com.caters.entity.Reviews;

import java.util.List;
import java.util.Optional;

public interface ReviewService {

    Reviews submitReview(Customers customer, ReviewDto dto);

    List<Reviews> getReviewsForCaterer(Caterers caterer);

    Optional<Reviews> getReviewByBookingId(Long bookingId);

    Double getAverageRatingForCaterer(Long catererId);
    List<Reviews> getReviewsByCustomer(Customers customer);
}