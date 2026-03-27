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
import java.util.List;

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
                List<AppointmentDto> appointments = appointmentService
                        .getAppointmentViewsForStylist(principal.getUserId());

                LocalDateTime now = LocalDateTime.now();
                LocalDate today = now.toLocalDate();

                List<AppointmentDto> upcomingAppointments = appointments.stream()
                        .filter(a -> a.getStatus() == AppointmentStatus.Booked
                                && (a.getSlotStartDateTime() == null || !a.getSlotStartDateTime().isBefore(now)))
                        .sorted(Comparator.comparing(AppointmentDto::getSlotStartDateTime,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();

                List<AppointmentDto> pastAppointments = appointments.stream()
                        .filter(a -> a.getStatus() != AppointmentStatus.Booked
                                || (a.getSlotStartDateTime() != null && a.getSlotStartDateTime().isBefore(now)))
                        .sorted(Comparator.comparing(AppointmentDto::getSlotStartDateTime,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .toList();

                model.addAttribute("upcomingAppointments", upcomingAppointments);
                model.addAttribute("pastAppointments", pastAppointments);
                model.addAttribute("availabilitySlots",
                        availabilitySlotService.getSlotsForStylist(principal.getUserId()));

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
                model.addAttribute("appointments",
                        appointmentService.getAppointmentViewsForCustomer(principal.getUserId()));
                model.addAttribute("services", serviceRepository.findAll());
                model.addAttribute("stylists", userService.getAllStylistDtos());
                yield "dashboard/customer";
            }
        };
    }
}
