package com.caters.service;

import com.caters.dto.DishDto;
import com.caters.entity.Caterers;
import com.caters.entity.Dishes;
import com.caters.repository.DishesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class DishServiceImpl implements DishService {

    private final DishesRepository dishesRepository;
    private static final String UPLOAD_DIR = "uploads/dishes/";

    public DishServiceImpl(DishesRepository dishesRepository) {
        this.dishesRepository = dishesRepository;
    }

    @Override
    public List<Dishes> getDishesByCaterer(Caterers caterer) {
        return dishesRepository.findByCatererOrderByIdDesc(caterer);
    }

    @Override
    @Transactional
    public void toggleAvailability(Long dishId, Caterers caterer) {
        Dishes dish = dishesRepository.findByIdAndCaterer(dishId, caterer)
                .orElseThrow(() -> new RuntimeException("Dish not found or unauthorized"));
        dish.setAvailable(!dish.isAvailable());
        dishesRepository.save(dish);
    }

    @Override
    @Transactional
    public void deleteDish(Long dishId, Caterers caterer) {
        Dishes dish = dishesRepository.findByIdAndCaterer(dishId, caterer)
                .orElseThrow(() -> new RuntimeException("Dish not found or unauthorized"));

        // Delete physical file from disk if it was an uploaded local image
        deletePhysicalFile(dish.getImagePath());

        dishesRepository.delete(dish);
    }

    @Override
    @Transactional
    public Dishes addDish(Caterers caterer, DishDto dishDto) {
        Dishes dish = new Dishes();
        dish.setCaterer(caterer);
        dish.setName(dishDto.getName());
        dish.setDescription(dishDto.getDescription());
        dish.setCourseType(dishDto.getCourseType());
        dish.setPrice(dishDto.getPrice());
        dish.setExtraCost(dishDto.getExtraCost() != null ? dishDto.getExtraCost() : BigDecimal.ZERO);
        dish.setVeg(dishDto.isVeg());
        dish.setAvailable(true);

        // Handle local file upload
        if (dishDto.getImageFile() != null && !dishDto.getImageFile().isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFilename = dishDto.getImageFile().getOriginalFilename();
                String cleanFileName = UUID.randomUUID() + "_" + (originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "dish.jpg");
                Path filePath = uploadPath.resolve(cleanFileName);

                Files.copy(dishDto.getImageFile().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                dish.setImagePath("/uploads/dishes/" + cleanFileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save dish image: " + e.getMessage());
            }
        }

        return dishesRepository.save(dish);
    }

    private void deletePhysicalFile(String imagePath) {
        if (imagePath != null && imagePath.startsWith("/uploads/")) {
            try {
                String relativePath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
                Path filePath = Paths.get(relativePath);
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {
                // Ignore failure to prevent breaking the transaction if the file was already deleted
            }
        }
    }
}