package edu.sjsu.cmpe172.salon.repository;

import edu.sjsu.cmpe172.salon.model.Appointment;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AppointmentRepository {
    private final Map<Integer, Appointment> store = new HashMap<>();
    private int nextId = 1;

    public List<Appointment> findAll() {
        return new ArrayList<>(store.values());
    }

    public Appointment save(Appointment appointment) {
        if (appointment.getId() == 0) {
            appointment.setId(nextId++);
        }
        store.put(appointment.getId(), appointment);
        return appointment;
    }

    public Optional<Appointment> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }
}
