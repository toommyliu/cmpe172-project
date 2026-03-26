package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.service.ProviderScheduleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final ProviderScheduleService scheduleService;

    public HomeController(ProviderScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/")
    public String home(Model model) {
        int providerId = 1;
        model.addAttribute("weeklyHours", scheduleService.getWeeklyHoursByDay(providerId));
        model.addAttribute("dateOverrides", scheduleService.getDateOverrides(providerId));
        return "home/index";
    }
}
