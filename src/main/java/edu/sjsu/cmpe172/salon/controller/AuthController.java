package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.dto.AppointmentDto;
import edu.sjsu.cmpe172.salon.enums.AppointmentStatus;
import edu.sjsu.cmpe172.salon.model.Customer;
import edu.sjsu.cmpe172.salon.model.Provider;
import edu.sjsu.cmpe172.salon.model.Stylist;
import edu.sjsu.cmpe172.salon.repository.ProviderRepository;
import edu.sjsu.cmpe172.salon.repository.ServiceRepository;
import edu.sjsu.cmpe172.salon.security.SalonUserPrincipal;
import edu.sjsu.cmpe172.salon.service.AppointmentService;
import edu.sjsu.cmpe172.salon.service.AvailabilitySlotService;
import edu.sjsu.cmpe172.salon.service.ProviderScheduleService;
import edu.sjsu.cmpe172.salon.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController {
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final AvailabilitySlotService availabilitySlotService;
    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;
    private final ProviderScheduleService providerScheduleService;

    public AuthController(UserService userService,
            AppointmentService appointmentService,
            AvailabilitySlotService availabilitySlotService,
            ServiceRepository serviceRepository,
            ProviderRepository providerRepository,
            ProviderScheduleService providerScheduleService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.availabilitySlotService = availabilitySlotService;
        this.serviceRepository = serviceRepository;
        this.providerRepository = providerRepository;
        this.providerScheduleService = providerScheduleService;
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
                    phoneNumber == null ? null : phoneNumber.trim());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Account created for " + customer.getEmailAddress() + ". Please sign in.");
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
                int providerId = 1;
                model.addAttribute("provider", providerRepository.findById(providerId).orElseGet(() -> {
                    Provider provider = new Provider();
                    // since we only support one provider
                    // we can just hardcode the ID here
                    provider.setId(providerId);
                    return provider;
                }));
                model.addAttribute("weeklyHoursByDay", providerScheduleService.getWeeklyHoursByDay(providerId));
                model.addAttribute("dateOverrides", providerScheduleService.getDateOverrides(providerId));
                yield "dashboard/admin";
            }
            case Stylist -> {
                LocalDateTime now = LocalDateTime.now();
                LocalDate today = now.toLocalDate();
                addReconciliationNotice(
                        model,
                        appointmentService.reconcileCompletedAppointmentsForDashboard(
                                principal.getUserId(),
                                principal.getUserRole(),
                                now));
                addAvailabilityExpirationNotice(
                        model,
                        availabilitySlotService.reconcileExpiredAvailabilitySlotsForStylist(
                                principal.getUserId(),
                                now));

                List<AppointmentDto> appointments = appointmentService
                        .getAppointmentViewsForStylist(principal.getUserId());

                List<AppointmentDto> upcomingAppointments = appointments.stream()
                        .filter(a -> isUpcomingAppointment(a, now))
                        .sorted(Comparator.comparing(AppointmentDto::getSlotStartDateTime,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();

                List<AppointmentDto> pastAppointments = appointments.stream()
                        .filter(a -> isPastAppointment(a, now))
                        .sorted(Comparator.comparing(AppointmentDto::getSlotStartDateTime,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .toList();

                model.addAttribute("upcomingAppointments", upcomingAppointments);
                model.addAttribute("pastAppointments", pastAppointments);
                model.addAttribute("availabilitySlots",
                        availabilitySlotService.getSlotsForStylist(principal.getUserId()));
                model.addAttribute("appointmentStatusBySlotId", appointmentStatusBySlotId(appointments));
                model.addAttribute("appointmentCustomerNameBySlotId", appointmentCustomerNameBySlotId(appointments));

                long upcomingTodayCount = upcomingAppointments.stream()
                        .filter(a -> a.getSlotStartDateTime() != null
                                && a.getSlotStartDateTime().toLocalDate().equals(today))
                        .count();
                model.addAttribute("upcomingTodayCount", upcomingTodayCount);

                if (principal.getUser() instanceof Stylist stylist) {
                    serviceRepository.findById(stylist.getServiceId()).ifPresent(service -> {
                        model.addAttribute("stylistServiceName", service.getName());
                        model.addAttribute("stylistServiceDurationMinutes", service.getDurationMinutes());
                    });
                }
                yield "dashboard/stylist";
            }
            case Customer -> {
                LocalDateTime now = LocalDateTime.now();
                addReconciliationNotice(
                        model,
                        appointmentService.reconcileCompletedAppointmentsForDashboard(
                                principal.getUserId(),
                                principal.getUserRole(),
                                now));

                List<AppointmentDto> appointments = appointmentService
                        .getAppointmentViewsForCustomer(principal.getUserId());

                List<AppointmentDto> upcomingAppointments = appointments.stream()
                        .filter(a -> isUpcomingAppointment(a, now))
                        .sorted(Comparator.comparing(AppointmentDto::getSlotStartDateTime,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();

                List<AppointmentDto> historyAppointments = appointments.stream()
                        .sorted(Comparator.comparing(AppointmentDto::getSlotStartDateTime,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .toList();

                model.addAttribute("upcomingAppointments", upcomingAppointments);
                model.addAttribute("historyAppointments", historyAppointments);
                model.addAttribute("services", serviceRepository.findAll());
                model.addAttribute("stylists", userService.getAllStylistDtos());
                yield "dashboard/customer";
            }
        };
    }

    /**
     * Upcoming includes booked appointments until their scheduled slot has ended.
     */
    static boolean isUpcomingAppointment(AppointmentDto appointment, LocalDateTime now) {
        return appointment.getStatus() == AppointmentStatus.Booked
                && (appointment.getSlotEndDateTime() == null || appointment.getSlotEndDateTime().isAfter(now));
    }

    /**
     * History includes terminal appointments and booked appointments whose scheduled slot has ended.
     */
    static boolean isPastAppointment(AppointmentDto appointment, LocalDateTime now) {
        return appointment.getStatus() != AppointmentStatus.Booked
                || (appointment.getSlotEndDateTime() != null && !appointment.getSlotEndDateTime().isAfter(now));
    }

    /**
     * Uses the latest appointment record for each slot so reused slots show their current appointment state.
     */
    static Map<Integer, AppointmentStatus> appointmentStatusBySlotId(List<AppointmentDto> appointments) {
        Map<Integer, AppointmentDto> latestAppointmentBySlotId = latestAppointmentBySlotId(appointments);
        Map<Integer, AppointmentStatus> statusBySlotId = new HashMap<>();
        latestAppointmentBySlotId.forEach((slotId, appointment) -> statusBySlotId.put(slotId, appointment.getStatus()));
        return statusBySlotId;
    }

    static Map<Integer, String> appointmentCustomerNameBySlotId(List<AppointmentDto> appointments) {
        Map<Integer, String> customerNameBySlotId = new HashMap<>();
        latestAppointmentBySlotId(appointments).forEach((slotId, appointment) -> {
            String customerName = appointment.getCustomerName();
            if (customerName == null || customerName.isBlank()) {
                customerName = "Customer #" + appointment.getCustomerUserId();
            }
            customerNameBySlotId.put(slotId, customerName);
        });
        return customerNameBySlotId;
    }

    private static Map<Integer, AppointmentDto> latestAppointmentBySlotId(List<AppointmentDto> appointments) {
        Map<Integer, AppointmentDto> latestAppointmentBySlotId = new HashMap<>();
        for (AppointmentDto appointment : appointments) {
            if (appointment.getAvailabilitySlotId() <= 0 || appointment.getStatus() == null) {
                continue;
            }

            latestAppointmentBySlotId.merge(
                    appointment.getAvailabilitySlotId(),
                    appointment,
                    (existing, candidate) -> candidate.getId() > existing.getId() ? candidate : existing);
        }
        return latestAppointmentBySlotId;
    }

    /**
     * Preserves any existing success flash while surfacing automatic completion reconciliation.
     */
    private void addReconciliationNotice(Model model, int reconciledCount) {
        if (reconciledCount <= 0) {
            return;
        }

        String notice = reconciledCount + " past appointment(s) were automatically marked completed.";
        appendSuccessNotice(model, notice);
    }

    /**
     * Reports unused availability that moved out of the bookable window.
     */
    private void addAvailabilityExpirationNotice(Model model, int expiredCount) {
        if (expiredCount <= 0) {
            return;
        }

        String notice = expiredCount + " past availability slot(s) were automatically marked expired.";
        appendSuccessNotice(model, notice);
    }

    private void appendSuccessNotice(Model model, String notice) {
        Object existingSuccessMessage = model.asMap().get("successMessage");
        if (existingSuccessMessage instanceof String existingMessage && !existingMessage.isBlank()) {
            model.addAttribute("successMessage", existingMessage + " " + notice);
        } else {
            model.addAttribute("successMessage", notice);
        }
    }
}
