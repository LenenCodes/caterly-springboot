package com.caters.controller;

import com.caters.dto.CatererProfileDto;
import com.caters.entity.Caterers;
import com.caters.service.CatererService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/caterer")
public class CatererController {

    private final CatererService catererService;

    public CatererController(CatererService catererService) {
        this.catererService = catererService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Caterers caterer =
                catererService.getCatererByEmail(userDetails.getUsername());

        model.addAttribute("caterer", caterer);

        return "caterer-dashboard";
    }

    // 1. Show Edit Profile Form
    @GetMapping("/profile/edit")
    public String showEditProfileForm(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Caterers caterer =
                catererService.getCatererByEmail(userDetails.getUsername());

        CatererProfileDto dto = new CatererProfileDto();

        dto.setBusinessName(caterer.getBusinessName());
        dto.setPhone(caterer.getPhone());
        dto.setAddress(caterer.getAddress());
        dto.setDescription(caterer.getDescription());
        dto.setVideoUrl(caterer.getVideoUrl());

        model.addAttribute("caterer", caterer);
        model.addAttribute("profileDto", dto);

        return "caterer-profile-edit";
    }

    // 2. Process Profile + Image + Video Upload
    @PostMapping("/profile/edit")
    public String processEditProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute("profileDto") CatererProfileDto profileDto,
            RedirectAttributes redirectAttributes) {

        Caterers caterer =
                catererService.getCatererByEmail(userDetails.getUsername());

        try {

            catererService.updateCatererProfile(
                    caterer.getId(),
                    profileDto
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Profile and media updated successfully!"
            );

        } catch (IOException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Failed to upload the profile image or video. Please try again."
            );

            return "redirect:/caterer/profile/edit";
        }

        return "redirect:/caterer/dashboard";
    }}