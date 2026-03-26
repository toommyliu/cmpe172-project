package edu.sjsu.cmpe172.salon.repository.mapper;

import edu.sjsu.cmpe172.salon.dto.AppointmentDto;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class AppointmentDtoDataMapper {
    public AppointmentDto toDomain(ResultSet resultSet) throws SQLException {
        AppointmentDto appointment = new AppointmentDto();
        appointment.setId(resultSet.getInt("id"));
        appointment.setCustomerUserId(resultSet.getInt("customer_user_id"));
        appointment.setStylistUserId(resultSet.getInt("stylist_user_id"));
        appointment.setServiceId(resultSet.getInt("service_id"));
        appointment.setServiceName(resultSet.getString("service_name"));
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
}
