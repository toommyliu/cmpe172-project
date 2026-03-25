package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.enums.Speciality;
import edu.sjsu.cmpe172.salon.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/admin/users/assign-stylist")
    public String assignStylistRole(@RequestParam int userId,
                                    @RequestParam int specialityId,
                                    RedirectAttributes redirectAttributes) {
        Speciality speciality = Speciality.fromValue(specialityId);
        if (speciality == Speciality.None) {
            redirectAttributes.addFlashAttribute("errorMessage", "A valid speciality is required.");
            return "redirect:/admin/dashboard";
        }

        try {
            boolean success = userService.assignStylistRole(userId, speciality);
            if (!success) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
                return "redirect:/admin/dashboard";
            }
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "User #" + userId + " is now a stylist (" + speciality.toString() + ")."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
