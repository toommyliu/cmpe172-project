package edu.sjsu.cmpe172.salon.repository.mapper;

import edu.sjsu.cmpe172.salon.model.Appointment;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class AppointmentDataMapper {
    public Appointment toDomain(ResultSet resultSet) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(resultSet.getInt("id"));
        appointment.setCustomerUserId(resultSet.getInt("customer_user_id"));
        appointment.setStylistUserId(resultSet.getInt("stylist_user_id"));
        appointment.setServiceId(resultSet.getInt("service_id"));
        appointment.setAvailabilitySlotId(resultSet.getInt("availability_slot_id"));
        appointment.setCustomerName(resultSet.getString("customer_name"));
        appointment.setStylistName(resultSet.getString("stylist_name"));
        Timestamp slotStart = resultSet.getTimestamp("slot_start_datetime");
        Timestamp slotEnd = resultSet.getTimestamp("slot_end_datetime");
        if (slotStart != null) {
            appointment.setSlotStartDateTime(slotStart.toLocalDateTime());
        }
        if (slotEnd != null) {
            appointment.setSlotEndDateTime(slotEnd.toLocalDateTime());
        }
        return appointment;
    }

    public Appointment toDomainWithoutSlotColumns(ResultSet resultSet) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(resultSet.getInt("id"));
        appointment.setCustomerUserId(resultSet.getInt("customer_user_id"));
        appointment.setStylistUserId(resultSet.getInt("stylist_user_id"));
        appointment.setServiceId(resultSet.getInt("service_id"));
        appointment.setAvailabilitySlotId(resultSet.getInt("availability_slot_id"));
        return appointment;
    }

    public void bindForInsert(PreparedStatement statement, Appointment appointment) throws SQLException {
        statement.setInt(1, appointment.getCustomerUserId());
        statement.setInt(2, appointment.getStylistUserId());
        statement.setInt(3, appointment.getServiceId());
        statement.setInt(4, appointment.getAvailabilitySlotId());
    }

    public void bindForUpdate(PreparedStatement statement, Appointment appointment) throws SQLException {
        statement.setInt(1, appointment.getCustomerUserId());
        statement.setInt(2, appointment.getStylistUserId());
        statement.setInt(3, appointment.getServiceId());
        statement.setInt(4, appointment.getAvailabilitySlotId());
        statement.setInt(5, appointment.getId());
    }
}
