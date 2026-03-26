package edu.sjsu.cmpe172.salon.repository.mapper;

import edu.sjsu.cmpe172.salon.model.ProviderWeeklyHours;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;

@Component
public class ProviderWeeklyHoursDataMapper {
    public ProviderWeeklyHours toDomain(ResultSet resultSet) throws SQLException {
        ProviderWeeklyHours hours = new ProviderWeeklyHours();
        hours.setProviderId(resultSet.getInt("provider_id"));
        hours.setDayOfWeek(DayOfWeek.of(resultSet.getInt("day_of_week")));
        hours.setClosed(resultSet.getBoolean("is_closed"));

        Time openTime = resultSet.getTime("open_time");
        Time closeTime = resultSet.getTime("close_time");
        hours.setOpenTime(openTime == null ? null : openTime.toLocalTime());
        hours.setCloseTime(closeTime == null ? null : closeTime.toLocalTime());
        return hours;
    }

    public void bindForUpsert(PreparedStatement statement, ProviderWeeklyHours hours) throws SQLException {
        statement.setInt(1, hours.getProviderId());
        statement.setInt(2, hours.getDayOfWeek().getValue());
        statement.setBoolean(3, hours.isClosed());
        if (hours.getOpenTime() == null) {
            statement.setTime(4, null);
        } else {
            statement.setTime(4, Time.valueOf(hours.getOpenTime()));
        }
        if (hours.getCloseTime() == null) {
            statement.setTime(5, null);
        } else {
            statement.setTime(5, Time.valueOf(hours.getCloseTime()));
        }
    }
}
