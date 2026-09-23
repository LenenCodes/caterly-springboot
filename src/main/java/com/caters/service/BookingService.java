package com.caters.service;

import com.caters.dto.BookingRequestDto;
import com.caters.entity.Bookings;
import com.caters.entity.Caterers;
import com.caters.entity.Customers;
import com.caters.enums.BookingStatus;

import java.util.List;

public interface BookingService {

    Bookings createBooking(Customers customer, BookingRequestDto dto);

    List<Bookings> getBookingsByCustomer(Customers customer);

    List<Bookings> getBookingsByCaterer(Caterers caterer);

    Bookings getBookingDetailsById(Long bookingId);

    void updateBookingStatus(Long bookingId, BookingStatus newStatus, Caterers caterer);

    void cancelBookingByCustomer(Long bookingId, Customers customer, String reason);
}