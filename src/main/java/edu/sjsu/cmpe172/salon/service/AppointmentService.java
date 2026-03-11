package edu.sjsu.cmpe172.salon.service;

import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public List<Appointment> getAllAppointments() {
        return repository.findAll();
    }

    public Appointment createAppointment(Appointment appointment) {
        return repository.save(appointment);
    }
}
