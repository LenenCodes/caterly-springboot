package com.caters.service;

import com.caters.dto.PlatePackageDto;
import com.caters.entity.Caterers;
import com.caters.entity.Dishes;
import com.caters.entity.PlatePackages;
import com.caters.repository.DishesRepository;
import com.caters.repository.PlatePackagesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class PlatePackageServiceImpl implements PlatePackageService {

    private final PlatePackagesRepository platePackagesRepository;
    private final DishesRepository dishesRepository;

    public PlatePackageServiceImpl(PlatePackagesRepository platePackagesRepository,
                                   DishesRepository dishesRepository) {
        this.platePackagesRepository = platePackagesRepository;
        this.dishesRepository = dishesRepository;
    }

    @Override
    public List<PlatePackages> getPackagesByCaterer(Caterers caterer) {
        return platePackagesRepository.findByCatererOrderByIdDesc(caterer);
    }

    @Override
    public List<PlatePackages> getActivePackagesByCaterer(Caterers caterer) {
        // Uses JOIN FETCH query to eagerly load defaultDishes for popover hover & UI sync
        return platePackagesRepository.findActivePackagesWithDishesByCaterer(caterer);
    }

    @Override
    public PlatePackages getPackageByIdWithDishes(Long packageId) {
        return platePackagesRepository.findByIdWithDishes(packageId)
                .orElseThrow(() -> new RuntimeException("Plate package not found with ID: " + packageId));
    }

    @Override
    @Transactional
    public PlatePackages createPackage(Caterers caterer, PlatePackageDto dto) {
        PlatePackages platePackage = new PlatePackages();
        platePackage.setCaterer(caterer);
        platePackage.setPackageName(dto.getPackageName());
        platePackage.setDescription(dto.getDescription());
        platePackage.setBasePricePerPlate(dto.getBasePricePerPlate());
        platePackage.setMinPlates(dto.getMinPlates());
        platePackage.setActive(true);

        // Fetch and associate the selected default dishes
        if (dto.getDefaultDishIds() != null && !dto.getDefaultDishIds().isEmpty()) {
            List<Dishes> selectedDishes = dishesRepository.findAllById(dto.getDefaultDishIds());
            platePackage.setDefaultDishes(new HashSet<>(selectedDishes));
        }

        return platePackagesRepository.save(platePackage);
    }

    @Override
    @Transactional
    public PlatePackages updatePackage(Long packageId, Caterers caterer, PlatePackageDto dto) {
        PlatePackages platePackage = platePackagesRepository.findByIdAndCaterer(packageId, caterer)
                .orElseThrow(() -> new RuntimeException("Package not found or unauthorized"));

        platePackage.setPackageName(dto.getPackageName());
        platePackage.setDescription(dto.getDescription());
        platePackage.setBasePricePerPlate(dto.getBasePricePerPlate());
        platePackage.setMinPlates(dto.getMinPlates());
        platePackage.setActive(dto.isActive());

        // Update default dishes set
        if (dto.getDefaultDishIds() != null) {
            List<Dishes> selectedDishes = dishesRepository.findAllById(dto.getDefaultDishIds());
            platePackage.setDefaultDishes(new HashSet<>(selectedDishes));
        } else {
            platePackage.getDefaultDishes().clear();
        }

        return platePackagesRepository.save(platePackage);
    }

    @Override
    @Transactional
    public void togglePackageStatus(Long packageId, Caterers caterer) {
        PlatePackages platePackage = platePackagesRepository.findByIdAndCaterer(packageId, caterer)
                .orElseThrow(() -> new RuntimeException("Package not found or unauthorized"));
        platePackage.setActive(!platePackage.isActive());
        platePackagesRepository.save(platePackage);
    }

    @Override
    @Transactional
    public void deletePackage(Long packageId, Caterers caterer) {
        PlatePackages platePackage = platePackagesRepository.findByIdAndCaterer(packageId, caterer)
                .orElseThrow(() -> new RuntimeException("Package not found or unauthorized"));
        platePackagesRepository.delete(platePackage);
    }
}