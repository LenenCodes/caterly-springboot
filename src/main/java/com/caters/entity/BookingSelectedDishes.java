package com.caters.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_selected_dishes")
public class BookingSelectedDishes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Bookings booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dishes dish;

    @Column(name = "dish_name_snapshot", nullable = false, length = 120)
    private String dishNameSnapshot;

    @Column(name = "course_type", nullable = false, length = 40)
    private String courseType;

    @Column(name = "extra_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal extraCharge = BigDecimal.ZERO;

    public BookingSelectedDishes() {}

    public BookingSelectedDishes(Bookings booking, Dishes dish, String dishNameSnapshot,
                                String courseType, BigDecimal extraCharge) {
        this.booking = booking;
        this.dish = dish;
        this.dishNameSnapshot = dishNameSnapshot;
        this.courseType = courseType;
        this.extraCharge = extraCharge != null ? extraCharge : BigDecimal.ZERO;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Bookings getBooking() { return booking; }
    public void setBooking(Bookings booking) { this.booking = booking; }

    public Dishes getDish() { return dish; }
    public void setDish(Dishes dish) { this.dish = dish; }

    public String getDishNameSnapshot() { return dishNameSnapshot; }
    public void setDishNameSnapshot(String dishNameSnapshot) { this.dishNameSnapshot = dishNameSnapshot; }

    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }

    public BigDecimal getExtraCharge() { return extraCharge; }
    public void setExtraCharge(BigDecimal extraCharge) { this.extraCharge = extraCharge; }
}