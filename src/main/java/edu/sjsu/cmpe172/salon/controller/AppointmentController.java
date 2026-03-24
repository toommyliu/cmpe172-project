package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.enums.Speciality;
import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.model.Stylist;
import edu.sjsu.cmpe172.salon.service.AppointmentService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AppointmentController {
    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    // the dummy Stylist for now
    private Stylist getStylist() {
        var bob = new Stylist();
        bob.setFirstName("Bob");
        bob.setLastName("Smith");
        bob.setEmailAddress("bob.smith@gmail.com");
        bob.setPassword("password");
        bob.setSpeciality(Speciality.Coloring);
        bob.setId(100);
        return bob;
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("appointments", service.getAllAppointments());
        return "appointments/index";
    }

    @GetMapping("/appointments/new")
    public String newAppointment(Model model) {
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("formAction", "/appointments");
        model.addAttribute("pageTitle", "Create Appointment");
        return "appointments/form";
    }

    @PostMapping("/appointments")
    public String createAppointment(@ModelAttribute Appointment appointment, RedirectAttributes redirectAttributes) {
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
        return "booking/available-slots";
    }

    @GetMapping("/book-appointment")
    public String bookAppointment(Model model) {
        model.addAttribute("specialities", Arrays.stream(Speciality.values())
                .filter(s -> s != Speciality.None)
                .collect(Collectors.toList()));
        model.addAttribute("stylists", List.of(getStylist()));
        return "booking/book-appointment";
    }

    @GetMapping("/booking-confirmation")
    public String bookingConfirmation() {
        return "booking/booking-confirmation";
    }
}
