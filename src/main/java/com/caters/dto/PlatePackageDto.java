package com.caters.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PlatePackageDto {

    private Long id;

    @NotBlank(message = "Package name is required")
    private String packageName;

    private String description;

    @NotNull(message = "Base price per plate is required")
    @DecimalMin(value = "1.0", message = "Price per plate must be at least 1.00")
    private BigDecimal basePricePerPlate;

    @Min(value = 1, message = "Minimum plate count must be at least 1")
    private int minPlates = 20;

    private boolean active = true;

    // List of dish IDs selected by the caterer to bundle inside this plate
    @NotEmpty(message = "Select at least one default dish to include in this plate package")
    private List<Long> defaultDishIds = new ArrayList<>();

    public PlatePackageDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePricePerPlate() { return basePricePerPlate; }
    public void setBasePricePerPlate(BigDecimal basePricePerPlate) { this.basePricePerPlate = basePricePerPlate; }

    public int getMinPlates() { return minPlates; }
    public void setMinPlates(int minPlates) { this.minPlates = minPlates; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<Long> getDefaultDishIds() { return defaultDishIds; }
    public void setDefaultDishIds(List<Long> defaultDishIds) { this.defaultDishIds = defaultDishIds; }
}