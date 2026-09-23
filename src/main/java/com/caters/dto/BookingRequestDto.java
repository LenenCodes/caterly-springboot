package com.caters.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BookingRequestDto {

    @NotNull(message = "Package selection is required")
    private Long packageId;

    @NotNull(message = "Plate count is required")
    @Min(value = 1, message = "Plate count must be at least 1")
    private Integer guestPlateCount;

    @NotEmpty(message = "Please select at least one dish for your plate")
    private List<Long> selectedDishIds = new ArrayList<>();

    @NotNull(message = "Event date is required")
    @FutureOrPresent(message = "Event date must be today or in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventDate;

    @NotNull(message = "Event time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime eventTime;

    @NotBlank(message = "Venue address is required")
    private String venueAddress;

    private String specialNotes;

    // Computed totals sent along or verified on the backend
    private BigDecimal finalPricePerPlate;
    private BigDecimal totalAmount;

    public BookingRequestDto() {}

    // Getters and Setters
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }

    public Integer getGuestPlateCount() { return guestPlateCount; }
    public void setGuestPlateCount(Integer guestPlateCount) { this.guestPlateCount = guestPlateCount; }

    public List<Long> getSelectedDishIds() { return selectedDishIds; }
    public void setSelectedDishIds(List<Long> selectedDishIds) { this.selectedDishIds = selectedDishIds; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }

    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }

    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String specialNotes) { this.specialNotes = specialNotes; }

    public BigDecimal getFinalPricePerPlate() { return finalPricePerPlate; }
    public void setFinalPricePerPlate(BigDecimal finalPricePerPlate) { this.finalPricePerPlate = finalPricePerPlate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}