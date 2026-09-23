package com.caters.controller;

import com.caters.entity.Bookings;
import com.caters.entity.Caterers;
import com.caters.enums.BookingStatus;
import com.caters.service.BookingService;
import com.caters.service.CatererService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/caterer/bookings")
public class CatererBookingController {

    private final CatererService catererService;
    private final BookingService bookingService;

    public CatererBookingController(CatererService catererService, BookingService bookingService) {
        this.catererService = catererService;
        this.bookingService = bookingService;
    }

    // 1. List all incoming booking requests for the caterer
    @GetMapping
    public String showCatererBookings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Caterers caterer = catererService.getCatererByEmail(userDetails.getUsername());
        List<Bookings> bookings = bookingService.getBookingsByCaterer(caterer);

        model.addAttribute("caterer", caterer);
        model.addAttribute("bookings", bookings);
        return "caterer-bookings";
    }

    // 2. View details of a single incoming booking
    @GetMapping("/{id}")
    public String viewBookingDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long bookingId,
            Model model) {

        Caterers caterer = catererService.getCatererByEmail(userDetails.getUsername());
        Bookings booking = bookingService.getBookingDetailsById(bookingId);

        // Authorization guard: ensure booking belongs to this caterer
        if (!booking.getCaterer().getId().equals(caterer.getId())) {
            throw new SecurityException("Unauthorized access to this booking.");
        }

        model.addAttribute("caterer", caterer);
        model.addAttribute("booking", booking);
        return "caterer-booking-details";
    }

    // 3. Update Booking Status (ACCEPT, REJECT, COMPLETE)
    @PostMapping("/{id}/status")
    public String updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long bookingId,
            @RequestParam("status") BookingStatus status,
            @RequestParam(value = "redirectOrigin", required = false, defaultValue = "list") String redirectOrigin,
            RedirectAttributes redirectAttributes) {

        Caterers caterer = catererService.getCatererByEmail(userDetails.getUsername());

        try {
            bookingService.updateBookingStatus(bookingId, status, caterer);
            redirectAttributes.addFlashAttribute("successMessage", "Booking #" + bookingId + " status updated to " + status.getDisplayName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        if ("details".equalsIgnoreCase(redirectOrigin)) {
            return "redirect:/caterer/bookings/" + bookingId;
        }
        return "redirect:/caterer/bookings";
    }
}