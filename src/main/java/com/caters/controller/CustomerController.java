package com.caters.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.caters.dto.BookingRequestDto;
import com.caters.entity.Bookings;
import com.caters.entity.Caterers;
import com.caters.entity.Customers;
import com.caters.entity.Dishes;
import com.caters.entity.PlatePackages;
import com.caters.entity.Reviews;
import com.caters.enums.BookingStatus;
import com.caters.enums.CourseType;
import com.caters.repository.CaterersRepository;
import com.caters.repository.CustomerRepository;
import com.caters.repository.UsersRepository;
import com.caters.service.BookingService;
import com.caters.service.CatererService;
import com.caters.service.CustomerService;
import com.caters.service.DishService;
import com.caters.service.PlatePackageService;
import com.caters.service.ReviewService;

@Controller
@RequestMapping("/customer")
public class CustomerController {

	private final UsersRepository usersRepository;
	private final CustomerRepository customerRepository;
	private final CaterersRepository caterersRepository;

	public CustomerController(UsersRepository usersRepository, CustomerRepository customerRepository,
			CaterersRepository catererRepository) {
		this.usersRepository = usersRepository;
		this.customerRepository = customerRepository;
		this.caterersRepository = catererRepository;
	}

	@Autowired
	CatererService catererService;

	@Autowired
	CustomerService customerService;

	@Autowired
	PlatePackageService platePackageService;

	@Autowired
	DishService dishService;
	@Autowired
	ReviewService reviewService;
	@Autowired
	BookingService bookingService;

	@GetMapping("/caterer/{id}/menu")
	public String showCatererMenuAndCustomizer(@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable("id") Long catererId, Model model) {

		Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());
		Caterers caterer = catererService.getCatererById(catererId)
				.orElseThrow(() -> new RuntimeException("Caterer not found"));

		// Fetch active packages & dishes
		List<PlatePackages> packages = platePackageService.getActivePackagesByCaterer(caterer);
		List<Dishes> allAvailableDishes = dishService.getDishesByCaterer(caterer).stream().filter(Dishes::isAvailable)
				.toList();

		Map<CourseType, List<Dishes>> dishesByCourse = allAvailableDishes.stream()
				.collect(Collectors.groupingBy(Dishes::getCourseType));

		// Fetch reviews and aggregate rating score
		List<Reviews> reviews = reviewService.getReviewsForCaterer(caterer);
		Double averageRating = reviewService.getAverageRatingForCaterer(caterer.getId());

		model.addAttribute("customer", customer);
		model.addAttribute("caterer", caterer);
		model.addAttribute("packages", packages);
		model.addAttribute("dishesByCourse", dishesByCourse);
		model.addAttribute("reviews", reviews);
		model.addAttribute("averageRating", averageRating);
		model.addAttribute("bookingDto", new BookingRequestDto());

		return "customer-caterer-menu";
	}

	@GetMapping("/dashboard")
	public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// 1. Fetch customer profile
		Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());

		// 2. Fetch verified caterers
		List<Caterers> verifiedCaterers = caterersRepository.findByIsVerifiedTrue();

		// 3. Map average ratings and review counts per caterer
		Map<Long, Double> ratingsMap = verifiedCaterers.stream().collect(Collectors.toMap(Caterers::getId, c -> {
			Double avg = reviewService.getAverageRatingForCaterer(c.getId());
			return avg != null ? avg : 0.0;
		}, (existing, replacement) -> existing, () -> new java.util.HashMap<Long, Double>()));

		Map<Long, Integer> reviewCountMap = verifiedCaterers.stream()
				.collect(Collectors.toMap(Caterers::getId, c -> reviewService.getReviewsForCaterer(c).size()));

		// 4. Map any completed, unreviewed booking per caterer for direct rating access
		List<Bookings> customerCompletedBookings = bookingService.getBookingsByCustomer(customer).stream()
				.filter(b -> b.getStatus() == BookingStatus.COMPLETED).toList();

		List<Long> reviewedBookingIds = reviewService.getReviewsByCustomer(customer).stream()
				.map(r -> r.getBooking().getId()).toList();

		// catererId -> eligibleBookingId
		Map<Long, Long> eligibleBookingForReview = customerCompletedBookings.stream()
				.filter(b -> !reviewedBookingIds.contains(b.getId())).collect(Collectors
						.toMap(b -> b.getCaterer().getId(), Bookings::getId, (existing, replacement) -> existing // keep
																													// first
																													// if
																													// multiple
						));

		model.addAttribute("customer", customer);
		model.addAttribute("caterers", verifiedCaterers);
		model.addAttribute("ratingsMap", ratingsMap);
		model.addAttribute("reviewCountMap", reviewCountMap);
		model.addAttribute("eligibleBookingForReview", eligibleBookingForReview);

		return "customer-dashboard";
	}

	// 1. Show Customer Profile Edit Form
	@GetMapping("/profile/edit")
	public String showEditProfileForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());
		model.addAttribute("customer", customer);
		return "customer-profile-edit";
	}

	// 2. Process Customer Profile Update
	@PostMapping("/profile/edit")
	public String processEditProfile(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("name") String name, @RequestParam("phone") String phone,
			@RequestParam("address") String address, RedirectAttributes redirectAttributes) {

		try {
			Customers customer = customerService.getCustomerByEmail(userDetails.getUsername());
			customer.setName(name);
			customer.setPhone(phone);
			customer.setAddress(address);
			customerRepository.save(customer);

			redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
			return "redirect:/customer/profile/edit";
		}

		return "redirect:/customer/dashboard";
	}
}