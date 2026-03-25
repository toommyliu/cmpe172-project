package edu.sjsu.cmpe172.salon.repository;

import edu.sjsu.cmpe172.salon.model.Appointment;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    List<Appointment> findAll();

    Optional<Appointment> findById(int id);

    List<Appointment> findByCustomerUserId(int customerUserId);

    List<Appointment> findByStylistUserId(int stylistUserId);

    Appointment create(Appointment appointment);

    Appointment update(Appointment appointment);

    boolean deleteById(int id);
}
