package edu.sjsu.cmpe172.salon.repository.mapper;

import edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus;
import edu.sjsu.cmpe172.salon.model.AvailabilitySlot;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class AvailabilitySlotDataMapper {
    public AvailabilitySlot toDomain(ResultSet resultSet) throws SQLException {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(resultSet.getInt("id"));
        slot.setStylistUserId(resultSet.getInt("stylist_user_id"));
        slot.setStartDateTime(resultSet.getTimestamp("start_datetime").toLocalDateTime());
        slot.setEndDateTime(resultSet.getTimestamp("end_datetime").toLocalDateTime());
        slot.setStatus(AvailabilitySlotStatus.fromValue(resultSet.getInt("status")));
        return slot;
    }

    public void bindForInsert(PreparedStatement statement, AvailabilitySlot slot) throws SQLException {
        statement.setInt(1, slot.getStylistUserId());
        statement.setTimestamp(2, Timestamp.valueOf(slot.getStartDateTime()));
        statement.setTimestamp(3, Timestamp.valueOf(slot.getEndDateTime()));
        statement.setInt(4, slot.getStatus().getValue());
    }
}
