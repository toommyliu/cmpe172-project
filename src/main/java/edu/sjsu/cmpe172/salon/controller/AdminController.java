package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.model.Provider;
import edu.sjsu.cmpe172.salon.repository.ProviderRepository;
import edu.sjsu.cmpe172.salon.repository.ServiceRepository;
import edu.sjsu.cmpe172.salon.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Controller
public class AdminController {
    private final UserService userService;
    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;

    public AdminController(UserService userService,
                           ServiceRepository serviceRepository,
                           ProviderRepository providerRepository) {
        this.userService = userService;
        this.serviceRepository = serviceRepository;
        this.providerRepository = providerRepository;
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
                               @RequestParam(required = false) String openTime,
                               @RequestParam(required = false) String closeTime,
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
            provider.setOpenTime(parseDateTimeNullable(openTime));
            provider.setCloseTime(parseDateTimeNullable(closeTime));
            providerRepository.upsert(provider);
            redirectAttributes.addFlashAttribute("successMessage", "Provider information updated.");
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

    private Timestamp parseDateTimeNullable(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Timestamp.valueOf(LocalDateTime.parse(normalized));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date/time value.");
        }
    }
}
