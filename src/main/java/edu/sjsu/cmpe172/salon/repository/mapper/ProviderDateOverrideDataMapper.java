package edu.sjsu.cmpe172.salon.repository.mapper;

import edu.sjsu.cmpe172.salon.model.ProviderDateOverride;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

@Component
public class ProviderDateOverrideDataMapper {
    public ProviderDateOverride toDomain(ResultSet resultSet) throws SQLException {
        ProviderDateOverride override = new ProviderDateOverride();
        override.setId(resultSet.getInt("id"));
        override.setProviderId(resultSet.getInt("provider_id"));

        Date overrideDate = resultSet.getDate("override_date");
        override.setOverrideDate(overrideDate == null ? null : overrideDate.toLocalDate());

        override.setClosed(resultSet.getBoolean("is_closed"));
        Time openTime = resultSet.getTime("open_time");
        Time closeTime = resultSet.getTime("close_time");
        override.setOpenTime(openTime == null ? null : openTime.toLocalTime());
        override.setCloseTime(closeTime == null ? null : closeTime.toLocalTime());
        return override;
    }

    public void bindForUpsert(PreparedStatement statement, ProviderDateOverride override) throws SQLException {
        statement.setInt(1, override.getProviderId());
        statement.setDate(2, Date.valueOf(override.getOverrideDate()));
        statement.setBoolean(3, override.isClosed());
        if (override.getOpenTime() == null) {
            statement.setTime(4, null);
        } else {
            statement.setTime(4, Time.valueOf(override.getOpenTime()));
        }
        if (override.getCloseTime() == null) {
            statement.setTime(5, null);
        } else {
            statement.setTime(5, Time.valueOf(override.getCloseTime()));
        }
    }
}
