package edu.sjsu.cmpe172.salon.repository.mapper;

import edu.sjsu.cmpe172.salon.enums.AppointmentStatus;
import edu.sjsu.cmpe172.salon.model.Appointment;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class AppointmentDataMapper {
    public Appointment toDomain(ResultSet resultSet) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(resultSet.getInt("id"));
        appointment.setCustomerUserId(resultSet.getInt("customer_user_id"));
        appointment.setStylistUserId(resultSet.getInt("stylist_user_id"));
        appointment.setServiceId(resultSet.getInt("service_id"));
        appointment.setAvailabilitySlotId(resultSet.getInt("availability_slot_id"));
        appointment.setStatus(AppointmentStatus.fromValue(resultSet.getInt("status")));
        return appointment;
    }

    public void bindForInsert(PreparedStatement statement, Appointment appointment) throws SQLException {
        statement.setInt(1, appointment.getCustomerUserId());
        statement.setInt(2, appointment.getStylistUserId());
        statement.setInt(3, appointment.getServiceId());
        statement.setInt(4, appointment.getAvailabilitySlotId());
        statement.setInt(5, appointment.getStatus().getValue());
    }

    public void bindForUpdate(PreparedStatement statement, Appointment appointment) throws SQLException {
        statement.setInt(1, appointment.getCustomerUserId());
        statement.setInt(2, appointment.getStylistUserId());
        statement.setInt(3, appointment.getServiceId());
        statement.setInt(4, appointment.getAvailabilitySlotId());
        statement.setInt(5, appointment.getStatus().getValue());
        statement.setInt(6, appointment.getId());
    }
}
