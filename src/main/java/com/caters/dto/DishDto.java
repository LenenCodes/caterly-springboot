package com.caters.dto;

import com.caters.enums.CourseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

public class DishDto {

    private Long id;

    @NotBlank(message = "Dish name is required")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private CourseType courseType;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be 0 or positive")
    private BigDecimal price;

    private BigDecimal extraCost = BigDecimal.ZERO;

    private boolean veg = true;

    private MultipartFile imageFile;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CourseType getCourseType() { return courseType; }
    public void setCourseType(CourseType courseType) { this.courseType = courseType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getExtraCost() { return extraCost; }
    public void setExtraCost(BigDecimal extraCost) { this.extraCost = extraCost; }

    public boolean isVeg() { return veg; }
    public void setVeg(boolean veg) { this.veg = veg; }

    public MultipartFile getImageFile() { return imageFile; }
    public void setImageFile(MultipartFile imageFile) { this.imageFile = imageFile; }
}