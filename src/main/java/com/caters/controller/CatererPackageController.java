package com.caters.controller;

import com.caters.dto.PlatePackageDto;
import com.caters.entity.Caterers;
import com.caters.entity.Dishes;
import com.caters.enums.CourseType;
import com.caters.service.CatererService;
import com.caters.service.DishService;
import com.caters.service.PlatePackageService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/caterer/packages")
public class CatererPackageController {

    private final CatererService catererService;
    private final PlatePackageService platePackageService;
    private final DishService dishService;

    public CatererPackageController(CatererService catererService,
                                    PlatePackageService platePackageService,
                                    DishService dishService) {
        this.catererService = catererService;
        this.platePackageService = platePackageService;
        this.dishService = dishService;
    }

    @GetMapping
    public String showPackagesPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Caterers caterer = catererService.getCatererByEmail(userDetails.getUsername());
        List<Dishes> allDishes = dishService.getDishesByCaterer(caterer);

        // Group dishes by course type (Starters, Mains, Desserts, etc.) for easy checklist selection
        Map<CourseType, List<Dishes>> dishesByCourse = allDishes.stream()
                .filter(Dishes::isAvailable)
                .collect(Collectors.groupingBy(Dishes::getCourseType));

        model.addAttribute("caterer", caterer);
        model.addAttribute("packages", platePackageService.getPackagesByCaterer(caterer));
        model.addAttribute("dishesByCourse", dishesByCourse);

        if (!model.containsAttribute("packageDto")) {
            model.addAttribute("packageDto", new PlatePackageDto());
        }
        return "caterer-packages";
    }

    @PostMapping("/create")
    public String createPackage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("packageDto") PlatePackageDto packageDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        Caterers caterer = catererService.getCatererByEmail(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.packageDto", bindingResult);
            redirectAttributes.addFlashAttribute("packageDto", packageDto);
            redirectAttributes.addFlashAttribute("errorMessage", "Please fix the validation errors in the package form.");
            return "redirect:/caterer/packages";
        }

        platePackageService.createPackage(caterer, packageDto);
        redirectAttributes.addFlashAttribute("successMessage", "New plate package created successfully!");
        return "redirect:/caterer/packages";
    }

    @PostMapping("/{id}/toggle")
    public String togglePackage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long packageId,
            RedirectAttributes redirectAttributes) {

        Caterers caterer = catererService.getCatererByEmail(userDetails.getUsername());
        platePackageService.togglePackageStatus(packageId, caterer);
        redirectAttributes.addFlashAttribute("successMessage", "Package visibility updated!");
        return "redirect:/caterer/packages";
    }

    @PostMapping("/{id}/delete")
    public String deletePackage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long packageId,
            RedirectAttributes redirectAttributes) {

        Caterers caterer = catererService.getCatererByEmail(userDetails.getUsername());
        try {
            platePackageService.deletePackage(packageId, caterer);
            redirectAttributes.addFlashAttribute("successMessage", "Plate package removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete package as it has active bookings.");
        }
        return "redirect:/caterer/packages";
    }
}