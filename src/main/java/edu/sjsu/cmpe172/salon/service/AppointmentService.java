package edu.sjsu.cmpe172.salon.service;

import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public List<Appointment> getAllAppointments() {
        return repository.findAll();
    }

    public Optional<Appointment> getAppointmentById(int id) {
        return repository.findById(id);
    }

    public Appointment createAppointment(Appointment appointment) {
        return repository.create(appointment);
    }

    public Appointment updateAppointment(Appointment appointment) {
        return repository.update(appointment);
    }

    public boolean deleteAppointment(int id) {
        return repository.deleteById(id);
    }
}
