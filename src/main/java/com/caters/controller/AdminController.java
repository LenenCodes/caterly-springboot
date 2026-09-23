package com.caters.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.caters.entity.Bookings;
import com.caters.entity.Caterers;
import com.caters.entity.Customers;
import com.caters.service.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Matches templates/admin-dashboard.html (or dashboard.html)
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pendingCaterers", adminService.getPendingCaterers());
        model.addAttribute("verifiedCaterers", adminService.getVerifiedCaterers());
        model.addAttribute("totalCustomers", adminService.getTotalCustomerCount());
        return "admin-dashboard"; 
    }

    // Approval button action from dashboard
    @PostMapping("/caterers/{id}/verify")
    public String verifyCaterer(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.verifyCaterer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Caterer verified successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard#verified";
    }

    // Revoke access button action from dashboard
    @PostMapping("/caterers/{id}/revoke")
    public String revokeCaterer(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.revokeCaterer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Caterer access revoked.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard#pending";
    }

    // --- Customer Oversight ---

    @GetMapping("/customers")
    public String listCustomers(Model model) {
        List<Customers> customers = adminService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "admin-customers"; // matches templates/admin-customers.html
    }

    @GetMapping("/customers/{id}")
    public String viewCustomer(@PathVariable("id") Long id, Model model) {
        model.addAttribute("customer", adminService.getCustomerById(id));
        return "admin-customer-details"; // matches templates/admin-customer-detail.html
    }

    @PostMapping("/customers/{id}/edit")
    public String editCustomer(
            @PathVariable("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            RedirectAttributes redirectAttributes) {
        try {
            adminService.updateCustomer(id, name, phone, address);
            redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/customers/" + id;
    }

    @PostMapping("/customers/{id}/toggle-status")
    public String toggleCustomerStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleCustomerStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer account status toggled.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @PostMapping("/customers/{id}/delete")
    public String deleteCustomer(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer removed.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/customers";
    }

    // --- Caterer Full Directory ---

    @GetMapping("/caterers")
    public String listCaterers(Model model) {
        List<Caterers> caterers = adminService.getAllCaterers();
        model.addAttribute("caterers", caterers);
        return "admin-caterers"; // matches templates/admin-caterers.html
    }

    @GetMapping("/caterers/{id}")
    public String viewCaterer(@PathVariable("id") Long id, Model model) {
        model.addAttribute("caterer", adminService.getCatererById(id));
        return "admin-caterer-detail"; // matches templates/admin-caterer-detail.html
    }

    @PostMapping("/caterers/{id}/edit")
    public String editCaterer(
            @PathVariable("id") Long id,
            @RequestParam("businessName") String businessName,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("description") String description,
            RedirectAttributes redirectAttributes) {
        try {
            adminService.updateCaterer(id, businessName, phone, address, description);
            redirectAttributes.addFlashAttribute("successMessage", "Caterer profile updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/caterers/" + id;
    }

    @PostMapping("/caterers/{id}/toggle-status")
    public String toggleCatererAccountStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleCatererAccountStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Caterer account status toggled.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/caterers";
    }

    @PostMapping("/caterers/{id}/delete")
    public String deleteCaterer(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteCaterer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Caterer removed.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/caterers";
    }

    // --- Bookings Ledger ---

    @GetMapping("/bookings")
    public String listBookings(Model model) {
        List<Bookings> bookings = adminService.getAllBookings();
        model.addAttribute("bookings", bookings);
        return "admin-bookings"; // matches templates/admin-bookings.html
    }
}