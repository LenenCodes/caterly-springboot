package com.caters.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "plate_packages")
public class PlatePackages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caterer_id", nullable = false)
    private Caterers caterer;

    @Column(name = "package_name", nullable = false, length = 150)
    private String packageName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price_per_plate", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePricePerPlate;

    @Column(name = "min_plates", nullable = false)
    private int minPlates = 20;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Many-to-Many mapping to package_default_dishes join table
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "package_default_dishes",
        joinColumns = @JoinColumn(name = "package_id"),
        inverseJoinColumns = @JoinColumn(name = "dish_id")
    )
    private Set<Dishes> defaultDishes = new HashSet<>();

    public PlatePackages() {}

    public PlatePackages(Caterers caterer, String packageName, String description,
                         BigDecimal basePricePerPlate, int minPlates, boolean isActive) {
        this.caterer = caterer;
        this.packageName = packageName;
        this.description = description;
        this.basePricePerPlate = basePricePerPlate;
        this.minPlates = minPlates;
        this.isActive = isActive;
    }

    // Helper methods to manage bundled dishes
    public void addDish(Dishes dish) {
        this.defaultDishes.add(dish);
    }

    public void removeDish(Dishes dish) {
        this.defaultDishes.remove(dish);
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Caterers getCaterer() { return caterer; }
    public void setCaterer(Caterers caterer) { this.caterer = caterer; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePricePerPlate() { return basePricePerPlate; }
    public void setBasePricePerPlate(BigDecimal basePricePerPlate) { this.basePricePerPlate = basePricePerPlate; }

    public int getMinPlates() { return minPlates; }
    public void setMinPlates(int minPlates) { this.minPlates = minPlates; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Set<Dishes> getDefaultDishes() { return defaultDishes; }
    public void setDefaultDishes(Set<Dishes> defaultDishes) { this.defaultDishes = defaultDishes; }
}