package com.caters.controller;

import com.caters.dto.CatererRegisterDto;
import com.caters.dto.CustomerRegisterDto;
import com.caters.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ==========================================
    // 1. LOGIN & LANDING ROUTES
    // ==========================================

    @GetMapping({"/", "/login"})
    public String showLoginForm() {
        return "login"; // templates/login.html
    }

    @GetMapping("/showNewUserForm")
    public String showUserChoiceForm() {
        return "register-choice"; // templates/register-choice.html
    }

    // ==========================================
    // 2. CUSTOMER REGISTRATION
    // ==========================================

    @GetMapping("/register/customer")
    public String showCustomerRegistrationForm(Model model) {
        // Backing object for Thymeleaf form binding (th:object="${customerDto}")
        model.addAttribute("customerDto", new CustomerRegisterDto());
        return "register-customer"; // templates/register-customer.html
    }

    @PostMapping("/register/customer")
    public String processCustomerRegistration(
            @Valid @ModelAttribute("customerDto") CustomerRegisterDto customerDto,
            BindingResult bindingResult,
            Model model) {

        // Check for duplicate email in MySQL
        if (userService.isEmailAlreadyRegistered(customerDto.getEmail())) {
            bindingResult.rejectValue("email", "error.customerDto", "An account with this email already exists.");
        }

        // Return form if validation annotations (@NotBlank, @Size, @Email) fail
        if (bindingResult.hasErrors()) {
            return "register-customer";
        }

        // Save base Users entity and linked Customers profile
        userService.registerCustomer(customerDto);

        // Redirect to login page with success flag
        return "redirect:/login?registered=true";
    }

    // ==========================================
    // 3. CATERER REGISTRATION
    // ==========================================

    @GetMapping("/register/caterer")
    public String showCatererRegistrationForm(Model model) {
        // Backing object for Thymeleaf form binding (th:object="${catererDto}")
        model.addAttribute("catererDto", new CatererRegisterDto());
        return "register-caterer"; // templates/register-caterer.html
    }

    @PostMapping("/register/caterer")
    public String processCatererRegistration(
            @Valid @ModelAttribute("catererDto") CatererRegisterDto catererDto,
            BindingResult bindingResult,
            Model model) {

        // Check for duplicate email in MySQL
        if (userService.isEmailAlreadyRegistered(catererDto.getEmail())) {
            bindingResult.rejectValue("email", "error.catererDto", "An account with this email already exists.");
        }

        // Return form if validation fails
        if (bindingResult.hasErrors()) {
            return "register-caterer";
        }

        // Save base Users entity and linked Caterers profile (isVerified=false)
        userService.registerCaterer(catererDto);

        // Redirect to login page with success flag
        return "redirect:/login?registered=true";
    }
}