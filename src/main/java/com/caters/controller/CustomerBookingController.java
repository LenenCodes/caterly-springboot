package com.caters.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.caters.dto.BookingRequestDto;
import com.caters.dto.ReviewDto;
import com.caters.entity.Bookings;
import com.caters.entity.Caterers;
import com.caters.entity.Customers;
import com.caters.enums.BookingStatus;
import com.caters.repository.BookingsRepository;
import com.caters.repository.PlatePackagesRepository;
import com.caters.service.BookingService;
import com.caters.service.CatererService;
import com.caters.service.CustomerService;
import com.caters.service.ReviewService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/customer")
public class CustomerBookingController {

    private final CustomerService customerService;
    private final BookingService bookingService;
    private final CatererService catererService;
    private final BookingsRepository bookingsRepository;
    private final PlatePackagesRepository platePackagesRepository;
    private final ReviewService reviewService;

    public CustomerBookingController(CustomerService customerService,
                                     BookingService bookingService,
                                     CatererService catererService,
                                     BookingsRepository bookingsRepository,
                                     PlatePackagesRepository platePackagesRepository,
                                     ReviewService reviewService) {
        this.customerService = customerService;
        this.bookingService = bookingService;
        this.catererService = catererService;
        this.bookingsRepository = bookingsRepository;
        this.platePackagesRepository = platePackagesRepository;
        this.reviewService = reviewService;
    }

    // 1. Process Checkout & Persist Booking
    @PostMapping("/booking/checkout")
    public String processBookingCheckout(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("bookingDto") BookingRequestDto bookingDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());

        Long catererId = null;
        if (bookingDto.getPackageId() != null) {
            catererId = platePackagesRepository.findById(bookingDto.getPackageId())
                    .map(pkg -> pkg.getCaterer().getId())
                    .orElse(null);
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please complete all required booking details correctly.");
            return catererId != null ? "redirect:/customer/caterer/" + catererId + "/menu" : "redirect:/customer/dashboard";
        }

        try {
            Bookings createdBooking = bookingService.createBooking(customer, bookingDto);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Event booking placed successfully! Reference ID: #" + createdBooking.getId());
            return "redirect:/customer/orders";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return catererId != null ? "redirect:/customer/caterer/" + catererId + "/menu" : "redirect:/customer/dashboard";
        }
    }

    // 2. View Customer Booking History
    @GetMapping("/orders")
    public String showCustomerOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());
        List<Bookings> orders = bookingService.getBookingsByCustomer(customer);

        // Fetch all booking IDs that have already been reviewed by this customer
        List<Long> reviewedBookingIds = reviewService.getReviewsByCustomer(customer)
                .stream()
                .map(r -> r.getBooking().getId())
                .toList();

        model.addAttribute("customer", customer);
        model.addAttribute("orders", orders);
        model.addAttribute("reviewedBookingIds", reviewedBookingIds);
        return "customer-orders";
    }

    // 3. View Single Order Summary & Selected Plate Contents
    @GetMapping("/orders/{id}")
    public String showOrderDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long bookingId,
            Model model) {

        Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());
        Bookings booking = bookingService.getBookingDetailsById(bookingId);

        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("Unauthorized access to this booking.");
        }

        model.addAttribute("booking", booking);
        return "customer-order-details";
    }

    // 4. API Endpoint: Fetch Unavailable/Locked Dates for a Caterer
    @GetMapping("/caterer/{catererId}/booked-dates")
    @ResponseBody
    public List<String> getBookedDatesForCaterer(@PathVariable("catererId") Long catererId) {
        Caterers caterer = catererService.getCatererById(catererId)
                .orElseThrow(() -> new RuntimeException("Caterer not found with ID: " + catererId));

        List<BookingStatus> lockedStatuses = List.of(BookingStatus.ACCEPTED, BookingStatus.CONFIRMED);
        return bookingsRepository.findBookedDatesByCatererAndStatusIn(caterer, lockedStatuses)
                .stream()
                .map(LocalDate::toString)
                .toList();
    }

    // 5. Show Review Submission Form
    @GetMapping("/orders/{id}/review")
    public String showReviewForm(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long bookingId,
            Model model,
            RedirectAttributes redirectAttributes) {

        Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());
        Bookings booking = bookingService.getBookingDetailsById(bookingId);

        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("Unauthorized access.");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only completed bookings can be reviewed.");
            return "redirect:/customer/orders";
        }

        if (reviewService.getReviewByBookingId(bookingId).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You have already reviewed this booking.");
            return "redirect:/customer/orders";
        }

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setBookingId(bookingId);

        model.addAttribute("booking", booking);
        model.addAttribute("reviewDto", reviewDto);
        return "customer-review-form";
    }

    // 6. Process Review Submission
    @PostMapping("/orders/review")
    public String processReviewSubmission(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("reviewDto") ReviewDto reviewDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid star rating and feedback comment.");
            return "redirect:/customer/orders/" + reviewDto.getBookingId() + "/review";
        }

        try {
            reviewService.submitReview(customer, reviewDto);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you! Your review and rating have been posted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customer/orders/" + reviewDto.getBookingId() + "/review";
        }

        // Return back to orders dashboard so reviewed badge updates
        return "redirect:/customer/orders";
    }

    // 7. Show Payment Gateway Checkout Page
    @GetMapping("/orders/{id}/payment")
    public String showPaymentPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long bookingId,
            @RequestParam(name = "type", defaultValue = "ADVANCE") String paymentType,
            Model model,
            RedirectAttributes redirectAttributes) {

        Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());
        Bookings booking = bookingService.getBookingDetailsById(bookingId);

        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("Unauthorized access.");
        }

        // Guard: Disallow payments on terminated or closed orders
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REJECTED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment cannot be made for cancelled or rejected bookings.");
            return "redirect:/customer/orders";
        }

        // Guard: Disallow balance payment if advance hasn't been paid
        if ("BALANCE".equalsIgnoreCase(paymentType) && "UNPAID".equalsIgnoreCase(booking.getPaymentStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Initial advance payment must be made before settling balance.");
            return "redirect:/customer/orders";
        }

        // Guard: Disallow payments if already fully paid
        if (booking.isFullyPaid()) {
            redirectAttributes.addFlashAttribute("errorMessage", "This booking is already fully settled.");
            return "redirect:/customer/orders";
        }

        BigDecimal payableAmount;
        if ("BALANCE".equalsIgnoreCase(paymentType)) {
            payableAmount = booking.getRemainingBalance();
        } else {
            payableAmount = booking.getTotalAmount().multiply(new BigDecimal("0.25"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        model.addAttribute("booking", booking);
        model.addAttribute("payableAmount", payableAmount);
        model.addAttribute("paymentType", paymentType.toUpperCase());
        return "customer-payment";
    }

    // 8. Process Payment Confirmation (Server-Enforced Amount)
    @PostMapping("/orders/{id}/pay")
    public String processPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long bookingId,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(name = "paymentType", defaultValue = "ADVANCE") String paymentType,
            RedirectAttributes redirectAttributes) {

        Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());
        Bookings booking = bookingService.getBookingDetailsById(bookingId);

        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("Unauthorized access.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REJECTED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot process payment on cancelled or rejected bookings.");
            return "redirect:/customer/orders";
        }

        String txnId = "TXN-" + System.currentTimeMillis();
        BigDecimal amountPaid;

        if ("BALANCE".equalsIgnoreCase(paymentType)) {
            if ("UNPAID".equalsIgnoreCase(booking.getPaymentStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot pay balance before paying advance.");
                return "redirect:/customer/orders";
            }
            if (booking.isFullyPaid()) {
                redirectAttributes.addFlashAttribute("errorMessage", "This booking is already fully settled.");
                return "redirect:/customer/orders";
            }

            // Enforce server-side balance calculation
            amountPaid = booking.getRemainingBalance();

            booking.setFinalPaymentId(txnId);
            booking.setFinalPaymentMethod(paymentMethod);
            booking.setBalanceAmount(amountPaid);
            booking.setPaymentStatus("FULLY_PAID");
        } else {
            if (!"UNPAID".equalsIgnoreCase(booking.getPaymentStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Advance payment has already been made.");
                return "redirect:/customer/orders";
            }

            // Enforce server-side 25% calculation
            amountPaid = booking.getTotalAmount().multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP);

            booking.setPaymentId(txnId);
            booking.setPaymentMethod(paymentMethod);
            booking.setAdvanceAmount(amountPaid);
            booking.setPaymentStatus("ADVANCE_PAID");
            booking.setStatus(BookingStatus.CONFIRMED);
        }

        bookingsRepository.save(booking);

        redirectAttributes.addFlashAttribute("successMessage", 
                paymentType.toUpperCase() + " payment of ₹" + amountPaid + " processed successfully! Txn ID: " + txnId);
        return "redirect:/customer/orders/" + bookingId + "/payment-success?type=" + paymentType.toUpperCase();
    }

    // 9. Payment Success Receipt Voucher
    @GetMapping("/orders/{id}/payment-success")
    public String showPaymentSuccess(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long bookingId,
            @RequestParam(name = "type", defaultValue = "ADVANCE") String paymentType,
            Model model) {

        Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());
        Bookings booking = bookingService.getBookingDetailsById(bookingId);

        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("Unauthorized access.");
        }

        model.addAttribute("booking", booking);
        model.addAttribute("paymentType", paymentType.toUpperCase());
        return "customer-payment-success";
    }

    // 10. Customer Booking Cancellation
    @PostMapping("/orders/{id}/cancel")
    public String cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long bookingId,
            @RequestParam(name = "reason", required = false) String reason,
            RedirectAttributes redirectAttributes) {

        Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());

        try {
            bookingService.cancelBookingByCustomer(bookingId, customer, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Booking #" + bookingId + " was cancelled successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/customer/orders";
    }
}