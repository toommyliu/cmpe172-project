package edu.sjsu.cmpe172.salon.repository;

import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.dto.AppointmentDto;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    List<AppointmentDto> findAllViews();

    Optional<AppointmentDto> findViewById(int id);

    Optional<Appointment> findById(int id);

    List<AppointmentDto> findViewsByCustomerUserId(int customerUserId);

    List<AppointmentDto> findViewsByStylistUserId(int stylistUserId);

    Appointment create(Appointment appointment);

    Appointment createWithSlotReservation(Appointment appointment);

    Appointment update(Appointment appointment);

    boolean deleteById(int id);
}
