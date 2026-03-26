package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.repository.ServiceRepository;
import edu.sjsu.cmpe172.salon.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private final UserService userService;
    private final ServiceRepository serviceRepository;

    public AdminController(UserService userService, ServiceRepository serviceRepository) {
        this.userService = userService;
        this.serviceRepository = serviceRepository;
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
}
