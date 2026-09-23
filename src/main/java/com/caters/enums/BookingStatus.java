package com.caters.enums;

public enum BookingStatus {
    PENDING("Pending Confirmation"),
    ACCEPTED("Accepted by Caterer"),
    CONFIRMED("Booking Confirmed & Advance Paid"),
    REJECTED("Rejected by Caterer"),
    COMPLETED("Event Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}