package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.model.Customer;
import edu.sjsu.cmpe172.salon.repository.ServiceRepository;
import edu.sjsu.cmpe172.salon.security.SalonUserPrincipal;
import edu.sjsu.cmpe172.salon.service.AppointmentService;
import edu.sjsu.cmpe172.salon.service.AvailabilitySlotService;
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
    private final AppointmentService appointmentService;
    private final AvailabilitySlotService availabilitySlotService;
    private final ServiceRepository serviceRepository;

    public AuthController(UserService userService,
                          AppointmentService appointmentService,
                          AvailabilitySlotService availabilitySlotService,
                          ServiceRepository serviceRepository) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.availabilitySlotService = availabilitySlotService;
        this.serviceRepository = serviceRepository;
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
    public String dashboard(@AuthenticationPrincipal SalonUserPrincipal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        return switch (principal.getUserRole()) {
            case Admin -> {
                model.addAttribute("users", userService.getAllUsers());
                model.addAttribute("services", serviceRepository.findAll());
                yield "dashboard/admin";
            }
            case Stylist -> {
                model.addAttribute("appointments", appointmentService.getAppointmentsForStylist(principal.getUserId()));
                model.addAttribute("availabilitySlots", availabilitySlotService.getSlotsForStylist(principal.getUserId()));
                yield "dashboard/stylist";
            }
            case Customer -> {
                model.addAttribute("appointments", appointmentService.getAppointmentsForCustomer(principal.getUserId()));
                model.addAttribute("services", serviceRepository.findAll());
                model.addAttribute("stylists", userService.getAllStylists());
                yield "dashboard/customer";
            }
        };
    }
}
