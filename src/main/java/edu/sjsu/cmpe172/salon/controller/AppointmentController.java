package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.enums.UserRole;
import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.security.SalonUserPrincipal;
import edu.sjsu.cmpe172.salon.service.AppointmentService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class AppointmentController {
    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal SalonUserPrincipal principal, Model model) {
        if (principal.getUserRole() == UserRole.Admin) {
            model.addAttribute("appointments", service.getAllAppointments());
            model.addAttribute("pageTitle", "All Appointments");
            model.addAttribute("showManagementActions", true);
            return "appointments/index";
        } else {
            // For Stylists and Customers, redirect to their integrated dashboard
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/appointments/new")
    public String newAppointment(Model model) {
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("formAction", "/appointments");
        model.addAttribute("pageTitle", "Create Appointment");
        return "appointments/form";
    }

    @PostMapping("/appointments")
    public String createAppointment(@AuthenticationPrincipal SalonUserPrincipal principal,
                                    @ModelAttribute Appointment appointment,
                                    RedirectAttributes redirectAttributes) {
        if (principal.getUserRole() == UserRole.Customer) {
            appointment.setCustomerUserId(principal.getUserId());
        }
        service.createAppointment(appointment);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment created successfully.");
        return "redirect:/appointments";
    }

    @GetMapping("/appointments/{id}/edit")
    public String editAppointment(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        return service.getAppointmentById(id)
                .map(appointment -> {
                    model.addAttribute("appointment", appointment);
                    model.addAttribute("formAction", "/appointments/" + id);
                    model.addAttribute("pageTitle", "Edit Appointment");
                    return "appointments/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Appointment not found.");
                    return "redirect:/appointments";
                });
    }

    @PostMapping("/appointments/{id}")
    public String updateAppointment(@PathVariable int id, @ModelAttribute Appointment appointment,
                                    RedirectAttributes redirectAttributes) {
        appointment.setId(id);
        try {
            service.updateAppointment(appointment);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/appointments";
    }

    @PostMapping("/appointments/{id}/delete")
    public String deleteAppointment(@PathVariable int id, RedirectAttributes redirectAttributes) {
        if (service.deleteAppointment(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Appointment deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Appointment not found.");
        }
        return "redirect:/appointments";
    }

    @GetMapping("/available-slots")
    public String availableSlots() {
        return "redirect:/dashboard";
    }

    @GetMapping("/book-appointment")
    public String bookAppointment() {
        return "redirect:/dashboard";
    }

    @GetMapping("/booking-confirmation")
    public String bookingConfirmation() {
        return "redirect:/dashboard";
    }
}
