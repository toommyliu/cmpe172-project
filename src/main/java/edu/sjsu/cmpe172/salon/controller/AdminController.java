package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.model.Provider;
import edu.sjsu.cmpe172.salon.repository.ProviderRepository;
import edu.sjsu.cmpe172.salon.repository.ServiceRepository;
import edu.sjsu.cmpe172.salon.service.ProviderScheduleService;
import edu.sjsu.cmpe172.salon.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AdminController {
    private final UserService userService;
    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;
    private final ProviderScheduleService providerScheduleService;

    public AdminController(UserService userService,
                           ServiceRepository serviceRepository,
                           ProviderRepository providerRepository,
                           ProviderScheduleService providerScheduleService) {
        this.userService = userService;
        this.serviceRepository = serviceRepository;
        this.providerRepository = providerRepository;
        this.providerScheduleService = providerScheduleService;
    }

    @PostMapping("/admin/users/assign-stylist")
    public String assignStylistRole(@RequestParam int userId,
                                    @RequestParam int serviceId,
                                    RedirectAttributes redirectAttributes) {
        if (!serviceRepository.existsById(serviceId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "A valid service is required.");
            return "redirect:/dashboard";
        }

        try {
            boolean success = userService.assignStylistRole(userId, serviceId);
            if (!success) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
                return "redirect:/dashboard";
            }
            String serviceName = serviceRepository.findById(serviceId)
                    .map(service -> service.getName())
                    .orElse("Unknown");
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "User #" + userId + " is now a stylist (" + serviceName + ")."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/admin/provider")
    public String saveProvider(@RequestParam(defaultValue = "1") int id,
                               @RequestParam String name,
                               @RequestParam(required = false) String address,
                               @RequestParam(required = false) String phoneNumber,
                               @RequestParam(required = false) String emailAddress,
                               RedirectAttributes redirectAttributes) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Provider name is required.");
            return "redirect:/dashboard";
        }

        try {
            Provider provider = new Provider();
            provider.setId(id);
            provider.setName(normalizedName);
            provider.setAddress(normalizeNullable(address));
            provider.setPhoneNumber(normalizeNullable(phoneNumber));
            provider.setEmailAddress(normalizeNullable(emailAddress));
            providerRepository.upsert(provider);
            redirectAttributes.addFlashAttribute("successMessage", "Provider information updated.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/admin/provider/weekly-hours")
    public String saveWeeklyHours(@RequestParam(defaultValue = "1") int id,
                                  @RequestParam(required = false) List<String> closedDays,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        try {
            Set<String> closedDaySet = closedDays == null ? Set.of() : new HashSet<>(closedDays);
            List<WeeklyHoursInput> updates = new ArrayList<>();
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                boolean closed = closedDaySet.contains(dayOfWeek.name());
                String openValue = request.getParameter("openTime_" + dayOfWeek.name());
                String closeValue = request.getParameter("closeTime_" + dayOfWeek.name());
                updates.add(new WeeklyHoursInput(
                        dayOfWeek,
                        closed,
                        parseTimeNullable(openValue),
                        parseTimeNullable(closeValue)
                ));
            }

            for (WeeklyHoursInput update : updates) {
                providerScheduleService.upsertWeeklyHours(
                        id,
                        update.dayOfWeek(),
                        update.closed(),
                        update.openTime(),
                        update.closeTime()
                );
            }
            redirectAttributes.addFlashAttribute("successMessage", "Weekly schedule updated.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/admin/provider/date-overrides")
    public String upsertDateOverride(@RequestParam(defaultValue = "1") int id,
                                     @RequestParam String overrideDate,
                                     @RequestParam String overrideMode,
                                     @RequestParam(required = false) String openTime,
                                     @RequestParam(required = false) String closeTime,
                                     RedirectAttributes redirectAttributes) {
        try {
            LocalDate parsedDate = parseDate(overrideDate);
            boolean closed = "CLOSED".equalsIgnoreCase(overrideMode);
            LocalTime parsedOpenTime = closed ? null : parseRequiredTime(openTime, "Open time is required for custom hours.");
            LocalTime parsedCloseTime = closed ? null : parseRequiredTime(closeTime, "Close time is required for custom hours.");

            providerScheduleService.upsertDateOverride(id, parsedDate, closed, parsedOpenTime, parsedCloseTime);
            redirectAttributes.addFlashAttribute("successMessage", "Date override saved.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/admin/provider/date-overrides/{overrideId}/delete")
    public String deleteDateOverride(@PathVariable int overrideId,
                                     @RequestParam(defaultValue = "1") int id,
                                     RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = providerScheduleService.deleteDateOverride(overrideId, id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Date override deleted.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Date override not found.");
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private LocalTime parseTimeNullable(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalTime.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid time value.");
        }
    }

    private LocalTime parseRequiredTime(String value, String messageIfMissing) {
        LocalTime parsed = parseTimeNullable(value);
        if (parsed == null) {
            throw new IllegalArgumentException(messageIfMissing);
        }
        return parsed;
    }

    private LocalDate parseDate(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new IllegalArgumentException("Override date is required.");
        }
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid override date.");
        }
    }

    private record WeeklyHoursInput(DayOfWeek dayOfWeek,
                                    boolean closed,
                                    LocalTime openTime,
                                    LocalTime closeTime) {
    }
}
