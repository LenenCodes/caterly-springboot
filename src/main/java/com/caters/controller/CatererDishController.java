package com.caters.controller;

import com.caters.dto.DishDto;
import com.caters.entity.Caterers;
import com.caters.enums.CourseType;
import com.caters.service.CatererService;
import com.caters.service.DishService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/caterer/dishes")
public class CatererDishController {

    private final CatererService catererService;
    private final DishService dishService;

    public CatererDishController(CatererService catererService, DishService dishService) {
        this.catererService = catererService;
        this.dishService = dishService;
    }

    @GetMapping
    public String showDishCatalog(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Caterers caterer = catererService.getCatererByEmail(userDetails.getUsername());
        
        model.addAttribute("caterer", caterer);
        model.addAttribute("dishes", dishService.getDishesByCaterer(caterer));
        model.addAttribute("courseTypes", CourseType.values());
        
        if (!model.containsAttribute("dishDto")) {
            model.addAttribute("dishDto", new DishDto());
        }
        return "caterer-dishes";
    }

    @PostMapping("/add")
    public String addDish(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("dishDto") DishDto dishDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        Caterers caterer = catererService.getCatererByEmail(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.dishDto", bindingResult);
            redirectAttributes.addFlashAttribute("dishDto", dishDto);
            redirectAttributes.addFlashAttribute("errorMessage", "Please fix the validation errors in the dish form.");
            return "redirect:/caterer/dishes";
        }

        dishService.addDish(caterer, dishDto);
        redirectAttributes.addFlashAttribute("successMessage", "New dish added successfully to your catalog!");
        return "redirect:/caterer/dishes";
    }

    @PostMapping("/{id}/toggle")
    public String toggleDish(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long dishId,
            RedirectAttributes redirectAttributes) {

        Caterers caterer = catererService.getCatererByEmail(userDetails.getUsername());
        dishService.toggleAvailability(dishId, caterer);
        redirectAttributes.addFlashAttribute("successMessage", "Dish status updated successfully!");
        return "redirect:/caterer/dishes";
    }

    @PostMapping("/{id}/delete")
    public String deleteDish(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long dishId,
            RedirectAttributes redirectAttributes) {

        Caterers caterer = catererService.getCatererByEmail(userDetails.getUsername());
        try {
            dishService.deleteDish(dishId, caterer);
            redirectAttributes.addFlashAttribute("successMessage", "Dish removed from catalog.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete dish as it is part of existing plate packages.");
        }
        return "redirect:/caterer/dishes";
    }
}