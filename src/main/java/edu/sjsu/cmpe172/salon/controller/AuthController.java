package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.model.Customer;
import edu.sjsu.cmpe172.salon.security.SalonUserPrincipal;
import edu.sjsu.cmpe172.salon.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String emailAddress,
                           @RequestParam String password,
                           @RequestParam(required = false) String phoneNumber,
                           RedirectAttributes redirectAttributes) {
        if (firstName.isBlank() || lastName.isBlank() || emailAddress.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "All required fields must be provided.");
            return "redirect:/register";
        }

        try {
            Customer customer = userService.registerCustomer(
                    firstName.trim(),
                    lastName.trim(),
                    emailAddress.trim().toLowerCase(),
                    password,
                    phoneNumber == null ? null : phoneNumber.trim()
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Account created for " + customer.getEmailAddress() + ". Please sign in."
            );
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal SalonUserPrincipal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        return switch (principal.getUserRole()) {
            case Admin -> "redirect:/admin/dashboard";
            case Stylist -> "redirect:/stylist/dashboard";
            case Customer -> "redirect:/customer/dashboard";
        };
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "dashboard/admin";
    }

    @GetMapping("/stylist/dashboard")
    public String stylistDashboard() {
        return "dashboard/stylist";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "dashboard/customer";
    }
}
