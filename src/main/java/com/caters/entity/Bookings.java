package com.caters.entity;

import com.caters.enums.BookingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Bookings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customers customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "caterer_id", nullable = false)
	private Caterers caterer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "package_id", nullable = false)
	private PlatePackages platePackage;

	@Column(name = "final_price_per_plate")
	private BigDecimal finalPricePerPlate;

	@Column(name = "guest_plate_count", nullable = false)
	private int guestPlateCount;

	// Initial Advance Payment Details (25%)
	@Column(name = "advance_amount", precision = 12, scale = 2)
	private BigDecimal advanceAmount;

	@Column(name = "payment_id", length = 100)
	private String paymentId;

	@Column(name = "payment_method", length = 50)
	private String paymentMethod;

	// Balance Settlement Details (Remaining 75%)
	@Column(name = "balance_amount", precision = 12, scale = 2)
	private BigDecimal balanceAmount;

	@Column(name = "final_payment_id", length = 100)
	private String finalPaymentId;

	@Column(name = "final_payment_method", length = 50)
	private String finalPaymentMethod;

	@Column(name = "payment_status", length = 30)
	private String paymentStatus = "UNPAID"; // UNPAID, ADVANCE_PAID, FULLY_PAID

	@Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "event_date", nullable = false)
	private LocalDate eventDate;

	@Column(name = "event_time", nullable = false)
	private LocalTime eventTime;

	@Column(name = "venue_address", nullable = false, columnDefinition = "TEXT")
	private String venueAddress;

	@Column(name = "special_notes", columnDefinition = "TEXT")
	private String specialNotes;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private BookingStatus status = BookingStatus.PENDING;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<BookingSelectedDishes> selectedDishes = new ArrayList<>();

	public Bookings() {
	}

	public Bookings(Customers customer, Caterers caterer, PlatePackages platePackage, int guestPlateCount,
			BigDecimal finalPricePerPlate, BigDecimal totalAmount, LocalDate eventDate, LocalTime eventTime,
			String venueAddress, String specialNotes) {
		this.customer = customer;
		this.caterer = caterer;
		this.platePackage = platePackage;
		this.guestPlateCount = guestPlateCount;
		this.finalPricePerPlate = finalPricePerPlate;
		this.totalAmount = totalAmount;
		this.eventDate = eventDate;
		this.eventTime = eventTime;
		this.venueAddress = venueAddress;
		this.specialNotes = specialNotes;
		this.status = BookingStatus.PENDING;
		this.paymentStatus = "UNPAID";
		this.createdAt = LocalDateTime.now();
	}

	// Helper method to add dish items
	public void addSelectedDish(BookingSelectedDishes item) {
		selectedDishes.add(item);
		item.setBooking(this);
	}

	// Convenience calculation for remaining balance
	public BigDecimal getRemainingBalance() {
		if (totalAmount == null) return BigDecimal.ZERO;
		BigDecimal advance = advanceAmount != null ? advanceAmount : BigDecimal.ZERO;
		BigDecimal balance = balanceAmount != null ? balanceAmount : BigDecimal.ZERO;
		BigDecimal remaining = totalAmount.subtract(advance).subtract(balance);
		return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
	}

	public boolean isFullyPaid() {
		return "FULLY_PAID".equalsIgnoreCase(this.paymentStatus);
	}

	// ==========================================
	// GETTERS & SETTERS
	// ==========================================

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Customers getCustomer() {
		return customer;
	}

	public void setCustomer(Customers customer) {
		this.customer = customer;
	}

	public Caterers getCaterer() {
		return caterer;
	}

	public void setCaterer(Caterers caterer) {
		this.caterer = caterer;
	}

	public PlatePackages getPlatePackage() {
		return platePackage;
	}

	public void setPlatePackage(PlatePackages platePackage) {
		this.platePackage = platePackage;
	}

	public int getGuestPlateCount() {
		return guestPlateCount;
	}

	public void setGuestPlateCount(int guestPlateCount) {
		this.guestPlateCount = guestPlateCount;
	}

	public BigDecimal getFinalPricePerPlate() {
		return finalPricePerPlate;
	}

	public void setFinalPricePerPlate(BigDecimal finalPricePerPlate) {
		this.finalPricePerPlate = finalPricePerPlate;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDate eventDate) {
		this.eventDate = eventDate;
	}

	public LocalTime getEventTime() {
		return eventTime;
	}

	public void setEventTime(LocalTime eventTime) {
		this.eventTime = eventTime;
	}

	public String getVenueAddress() {
		return venueAddress;
	}

	public void setVenueAddress(String venueAddress) {
		this.venueAddress = venueAddress;
	}

	public String getSpecialNotes() {
		return specialNotes;
	}

	public void setSpecialNotes(String specialNotes) {
		this.specialNotes = specialNotes;
	}

	public BookingStatus getStatus() {
		return status;
	}

	public void setStatus(BookingStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public BigDecimal getAdvanceAmount() {
		return advanceAmount;
	}

	public void setAdvanceAmount(BigDecimal advanceAmount) {
		this.advanceAmount = advanceAmount;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public BigDecimal getBalanceAmount() {
		return balanceAmount;
	}

	public void setBalanceAmount(BigDecimal balanceAmount) {
		this.balanceAmount = balanceAmount;
	}

	public String getFinalPaymentId() {
		return finalPaymentId;
	}

	public void setFinalPaymentId(String finalPaymentId) {
		this.finalPaymentId = finalPaymentId;
	}

	public String getFinalPaymentMethod() {
		return finalPaymentMethod;
	}

	public void setFinalPaymentMethod(String finalPaymentMethod) {
		this.finalPaymentMethod = finalPaymentMethod;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public List<BookingSelectedDishes> getSelectedDishes() {
		return selectedDishes;
	}

	public void setSelectedDishes(List<BookingSelectedDishes> selectedDishes) {
		this.selectedDishes = selectedDishes;
	}
}