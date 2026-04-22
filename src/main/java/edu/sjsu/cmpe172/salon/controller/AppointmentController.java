package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.enums.UserRole;
import edu.sjsu.cmpe172.salon.exception.SlotReservationConflictException;
import edu.sjsu.cmpe172.salon.dto.AppointmentDto;
import edu.sjsu.cmpe172.salon.dto.MockNotificationResponse;
import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.model.Stylist;
import edu.sjsu.cmpe172.salon.repository.ServiceRepository;
import edu.sjsu.cmpe172.salon.security.SalonUserPrincipal;
import edu.sjsu.cmpe172.salon.service.AppointmentService;
import edu.sjsu.cmpe172.salon.service.AvailabilitySlotService;
import edu.sjsu.cmpe172.salon.service.NotificationGatewayService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class AppointmentController {
    private final AppointmentService service;
    private final AvailabilitySlotService availabilitySlotService;
    private final ServiceRepository serviceRepository;
    private final NotificationGatewayService notificationGatewayService;

    public AppointmentController(AppointmentService service,
                                 AvailabilitySlotService availabilitySlotService,
                                 ServiceRepository serviceRepository,
                                 NotificationGatewayService notificationGatewayService) {
        this.service = service;
        this.availabilitySlotService = availabilitySlotService;
        this.serviceRepository = serviceRepository;
        this.notificationGatewayService = notificationGatewayService;
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal SalonUserPrincipal principal, Model model) {
        if (principal.getUserRole() == UserRole.Admin) {
            model.addAttribute("appointments", service.getAllAppointmentViews());
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
        model.addAttribute("services", serviceRepository.findAll());
        return "appointments/form";
    }

    @PostMapping("/appointments")
    public String createAppointment(@AuthenticationPrincipal SalonUserPrincipal principal,
                                    @ModelAttribute Appointment appointment,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        if (principal.getUserRole() == UserRole.Customer) {
            appointment.setCustomerUserId(principal.getUserId());
        }
        try {
            Appointment createdAppointment = service.createAppointment(appointment);

            try {
                AppointmentDto createdAppointmentView = service.getAppointmentViewById(createdAppointment.getId())
                        .orElseThrow(() -> new IllegalStateException("Unable to load created appointment details."));
                MockNotificationResponse notificationResponse = notificationGatewayService.sendAppointmentConfirmation(createdAppointmentView);

                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Appointment created successfully. Confirmation notification status: " + notificationResponse.getStatus() + "."
                );
            } catch (Exception notificationException) {
                redirectAttributes.addFlashAttribute("successMessage", "Appointment created successfully.");
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Appointment was created, but confirmation notification could not be sent."
                );
            }
        } catch (SlotReservationConflictException ex) {
            redirectAttributes.addFlashAttribute("bookingConflict", true);
            return "redirect:/dashboard?tab=book-now";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/appointments/{id}/edit")
    public String editAppointment(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        return service.getAppointmentById(id)
                .map(appointment -> {
                    model.addAttribute("appointment", appointment);
                    model.addAttribute("formAction", "/appointments/" + id);
                    model.addAttribute("pageTitle", "Edit Appointment");
                    model.addAttribute("services", serviceRepository.findAll());
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

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable int id,
                                    @AuthenticationPrincipal SalonUserPrincipal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        try {
            if (service.cancelAppointment(id, principal.getUserId())) {
                redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Appointment not found.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/appointments/{id}/complete")
    public String completeAppointment(@PathVariable int id,
                                      @AuthenticationPrincipal SalonUserPrincipal principal,
                                      RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        try {
            if (service.completeAppointment(id, principal.getUserId())) {
                redirectAttributes.addFlashAttribute("successMessage", "Appointment marked as completed.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Appointment not found.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
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

    @GetMapping("/customer/stylists/{stylistId}/available-slots")
    @ResponseBody
    public List<Map<String, String>> getAvailableSlotsForStylist(@PathVariable int stylistId,
                                                                  @RequestParam(required = false) Integer serviceId,
                                                                  @AuthenticationPrincipal SalonUserPrincipal principal) {
        if (principal == null || (principal.getUserRole() != UserRole.Customer && principal.getUserRole() != UserRole.Admin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access.");
        }

        final Integer expectedDurationMinutes = serviceId == null
                ? null
                : serviceRepository.findById(serviceId)
                .map(edu.sjsu.cmpe172.salon.model.Service::getDurationMinutes)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid service selected."));

        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("EEE, MMM d h:mm a");
        DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("h:mm a");
        return availabilitySlotService.getAvailableSlotsForStylist(stylistId)
                .stream()
                .filter(slot -> expectedDurationMinutes == null
                        || Duration.between(slot.getStartDateTime(), slot.getEndDateTime()).toMinutes() == expectedDurationMinutes)
                .map(slot -> Map.of(
                        "id", String.valueOf(slot.getId()),
                        "startDateTime", slot.getStartDateTime().toString(),
                        "endDateTime", slot.getEndDateTime().toString(),
                        "label", slot.getStartDateTime().format(labelFormatter),
                        "startLabel", slot.getStartDateTime().format(labelFormatter),
                        "endLabel", slot.getEndDateTime().format(endFormatter),
                        "rangeLabel", slot.getStartDateTime().format(labelFormatter) + " - " + slot.getEndDateTime().format(endFormatter),
                        "durationMinutes", String.valueOf(Duration.between(slot.getStartDateTime(), slot.getEndDateTime()).toMinutes())
                ))
                .toList();
    }

    @PostMapping("/stylist/availability")
    public String createAvailabilitySlot(@AuthenticationPrincipal SalonUserPrincipal principal,
                                         @RequestParam String startDateTime,
                                         RedirectAttributes redirectAttributes) {
        if (principal == null || principal.getUserRole() != UserRole.Stylist) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized action.");
            return "redirect:/dashboard";
        }

        if (!(principal.getUser() instanceof Stylist stylist)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to resolve stylist profile.");
            return "redirect:/dashboard";
        }

        try {
            LocalDateTime start = parseDateTimeInput(startDateTime);
            int durationMinutes = resolveServiceDurationMinutes(stylist.getServiceId());
            LocalDateTime end = start.plusMinutes(durationMinutes);
            availabilitySlotService.createSlot(principal.getUserId(), start, end);
            redirectAttributes.addFlashAttribute("successMessage", "Availability slot created.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/stylist/availability/bulk")
    public String createBulkAvailabilitySlots(@AuthenticationPrincipal SalonUserPrincipal principal,
                                              @RequestParam String startDate,
                                              @RequestParam String endDate,
                                              @RequestParam String dayStartTime,
                                              @RequestParam String dayEndTime,
                                              @RequestParam(required = false) List<String> weekdays,
                                              RedirectAttributes redirectAttributes) {
        if (principal == null || principal.getUserRole() != UserRole.Stylist) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized action.");
            return "redirect:/dashboard";
        }

        if (!(principal.getUser() instanceof Stylist stylist)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to resolve stylist profile.");
            return "redirect:/dashboard";
        }

        try {
            LocalDate parsedStartDate = LocalDate.parse(startDate);
            LocalDate parsedEndDate = LocalDate.parse(endDate);
            LocalTime parsedDayStartTime = LocalTime.parse(dayStartTime);
            LocalTime parsedDayEndTime = LocalTime.parse(dayEndTime);
            Set<DayOfWeek> selectedWeekdays = parseWeekdays(weekdays);
            int slotDurationMinutes = resolveServiceDurationMinutes(stylist.getServiceId());

            AvailabilitySlotService.BulkCreateResult result = availabilitySlotService.createBulkSlotsForStylist(
                    principal.getUserId(),
                    parsedStartDate,
                    parsedEndDate,
                    selectedWeekdays,
                    parsedDayStartTime,
                    parsedDayEndTime,
                    slotDurationMinutes
            );

            String errorHint = result.createdCount() > 0 
                ? "<br><small>Some slots may have been skipped due to conflicts, invalid times, or being outside of provider hours.</small>" : "";
            if (result.createdCount() > 0) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Bulk availability created: " + result.createdCount() + " slot(s). Skipped: " + result.skippedCount() + "." + errorHint
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "No slots were created. Skipped: " + result.skippedCount() + "." + errorHint
                );
            }
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/stylist/availability/{slotId}/delete")
    public String cancelAvailabilitySlot(@AuthenticationPrincipal SalonUserPrincipal principal,
                                         @PathVariable int slotId,
                                         RedirectAttributes redirectAttributes) {
        if (principal == null || principal.getUserRole() != UserRole.Stylist) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized action.");
            return "redirect:/dashboard";
        }

        boolean cancelled = availabilitySlotService.cancelSlot(slotId, principal.getUserId());
        if (cancelled) {
            redirectAttributes.addFlashAttribute("successMessage", "Availability slot cancelled.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Only available slots can be cancelled.");
        }
        return "redirect:/dashboard";
    }

    private LocalDateTime parseDateTimeInput(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid date-time format.");
            }
        }
    }

    private Set<DayOfWeek> parseWeekdays(List<String> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) {
            throw new IllegalArgumentException("Select at least one weekday.");
        }

        java.util.Set<DayOfWeek> parsedDays = new java.util.HashSet<>();
        for (String day : weekdays) {
            try {
                parsedDays.add(DayOfWeek.valueOf(day));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid weekday value.");
            }
        }
        return parsedDays;
    }

    private int resolveServiceDurationMinutes(int serviceId) {
        return serviceRepository.findById(serviceId)
                .map(edu.sjsu.cmpe172.salon.model.Service::getDurationMinutes)
                .orElseThrow(() -> new IllegalArgumentException("Assigned service not found."));
    }
}
