package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.enums.Speciality;
import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.model.Customer;
import edu.sjsu.cmpe172.salon.model.Stylist;
import edu.sjsu.cmpe172.salon.service.AppointmentService;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.Arrays;

@Controller
public class AppointmentController {
    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    // the dummy Customer for now
    private Customer getCustomer() {
        var alice = new Customer();
        alice.setFirstName("Alice");
        alice.setLastName("Woods");
        alice.setEmailAddress("alice.woods@gmail.com");
        alice.setPassword("password");
        alice.setPhoneNumber("123-456-7890");
        alice.setId(1);
        return alice;
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

    @PostConstruct
    public void init() {
        var alice = getCustomer();
        var bob = getStylist();

        // generate a random speciality
        List<Speciality> supported = Arrays.stream(Speciality.values())
                .filter(s -> s != Speciality.None)
                .collect(Collectors.toList());
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            Speciality randomValue = supported.get(random.nextInt(supported.size()));
            service.createAppointment(new Appointment(i, alice.getId(), bob.getId(), randomValue.getValue(), i + 1));
        }
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        List<Appointment> appointments = service.getAllAppointments();
        model.addAttribute("helper", new AppointmentHelper(appointments));
        model.addAttribute("user", getCustomer());
        model.addAttribute("stylist", getStylist());
        return "appointments";
    }

    @GetMapping("/available-slots")
    public String availableSlots() {
        return "available-slots";
    }

    @GetMapping("/book-appointment")
    public String bookAppointment(Model model) {
        model.addAttribute("specialities", Arrays.stream(Speciality.values())
                .filter(s -> s != Speciality.None)
                .collect(Collectors.toList()));
        model.addAttribute("stylists", List.of(getStylist()));
        return "book-appointment";
    }

    @GetMapping("/booking-confirmation")
    public String bookingConfirmation() {
        return "booking-confirmation";
    }

    public static class AppointmentHelper {
        private final List<Appointment> appointments;

        public AppointmentHelper(List<Appointment> appointments) {
            this.appointments = appointments;
        }

        public List<Appointment> getAppointments() {
            return appointments;
        }
    }
}
