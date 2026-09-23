package com.caters.entity;

import com.caters.enums.CourseType;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "dishes")
public class Dishes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caterer_id", nullable = false)
    private Caterers caterer;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false, length = 40)
    private CourseType courseType;

    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    // Supports both local paths (e.g., /uploads/dishes/...) and external web URLs (https://...)
    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "is_veg", nullable = false)
    private boolean isVeg = true;

    @Column(name = "extra_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal extraCost = BigDecimal.ZERO;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    // No-arg constructor required by JPA
    public Dishes() {}

    public Dishes(Caterers caterer, String name, String description, CourseType courseType, 
                  BigDecimal price, String imagePath, boolean isVeg, BigDecimal extraCost, boolean isAvailable) {
        this.caterer = caterer;
        this.name = name;
        this.description = description;
        this.courseType = courseType;
        this.price = price;
        this.imagePath = imagePath;
        this.isVeg = isVeg;
        this.extraCost = extraCost;
        this.isAvailable = isAvailable;
    }

    // Helper: Checks if image is an external web link or local file
    public boolean isExternalImage() {
        return this.imagePath != null && (this.imagePath.startsWith("http://") || this.imagePath.startsWith("https://"));
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Caterers getCaterer() { return caterer; }
    public void setCaterer(Caterers caterer) { this.caterer = caterer; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CourseType getCourseType() { return courseType; }
    public void setCourseType(CourseType courseType) { this.courseType = courseType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public boolean isVeg() { return isVeg; }
    public void setVeg(boolean veg) { isVeg = veg; }

    public BigDecimal getExtraCost() { return extraCost; }
    public void setExtraCost(BigDecimal extraCost) { this.extraCost = extraCost; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}