package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.model.ProviderWeeklyHours;
import edu.sjsu.cmpe172.salon.repository.ProviderWeeklyHoursRepository;
import edu.sjsu.cmpe172.salon.repository.mapper.ProviderWeeklyHoursDataMapper;
import edu.sjsu.cmpe172.salon.repository.sql.ProviderWeeklyHoursSql;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MySqlProviderWeeklyHoursRepository implements ProviderWeeklyHoursRepository {
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final ProviderWeeklyHoursDataMapper dataMapper;

    public MySqlProviderWeeklyHoursRepository(@Value("${salon.db.url}") String dbUrl,
                                              @Value("${salon.db.username}") String dbUsername,
                                              @Value("${salon.db.password}") String dbPassword,
                                              ProviderWeeklyHoursDataMapper dataMapper) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.dataMapper = dataMapper;
        ensureSchema();
    }

    @Override
    public List<ProviderWeeklyHours> findByProviderId(int providerId) {
        List<ProviderWeeklyHours> hours = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ProviderWeeklyHoursSql.FIND_BY_PROVIDER_ID)) {
            statement.setInt(1, providerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    hours.add(dataMapper.toDomain(resultSet));
                }
            }
            return hours;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read provider weekly hours", ex);
        }
    }

    @Override
    public void upsert(ProviderWeeklyHours hours) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ProviderWeeklyHoursSql.UPSERT)) {
            dataMapper.bindForUpsert(statement, hours);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save provider weekly hours", ex);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    private void ensureSchema() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(ProviderWeeklyHoursSql.CREATE_TABLE);
            seedDefaultWeek(connection, 1);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize provider weekly hours schema", ex);
        }
    }

    private void seedDefaultWeek(Connection connection, int providerId) throws SQLException {
        int rowCount;
        try (PreparedStatement countStatement = connection.prepareStatement(ProviderWeeklyHoursSql.COUNT_BY_PROVIDER_ID)) {
            countStatement.setInt(1, providerId);
            try (ResultSet resultSet = countStatement.executeQuery()) {
                rowCount = resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }

        if (rowCount > 0) {
            return;
        }

        try (PreparedStatement upsertStatement = connection.prepareStatement(ProviderWeeklyHoursSql.UPSERT)) {
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                ProviderWeeklyHours hours = new ProviderWeeklyHours();
                hours.setProviderId(providerId);
                hours.setDayOfWeek(dayOfWeek);
                hours.setClosed(false);
                hours.setOpenTime(java.time.LocalTime.of(9, 0));
                hours.setCloseTime(java.time.LocalTime.of(17, 0));
                dataMapper.bindForUpsert(upsertStatement, hours);
                upsertStatement.addBatch();
            }
            upsertStatement.executeBatch();
        }
    }
}
