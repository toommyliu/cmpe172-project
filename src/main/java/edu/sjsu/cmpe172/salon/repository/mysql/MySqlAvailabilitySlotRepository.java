package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus;
import edu.sjsu.cmpe172.salon.model.AvailabilitySlot;
import edu.sjsu.cmpe172.salon.repository.AvailabilitySlotRepository;
import edu.sjsu.cmpe172.salon.repository.mapper.AvailabilitySlotDataMapper;
import edu.sjsu.cmpe172.salon.repository.sql.AvailabilitySlotSql;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MySqlAvailabilitySlotRepository implements AvailabilitySlotRepository {
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final AvailabilitySlotDataMapper dataMapper;

    public MySqlAvailabilitySlotRepository(@Value("${salon.db.url}") String dbUrl,
                                           @Value("${salon.db.username}") String dbUsername,
                                           @Value("${salon.db.password}") String dbPassword,
                                           AvailabilitySlotDataMapper dataMapper) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.dataMapper = dataMapper;
        ensureSchema();
    }

    @Override
    public Optional<AvailabilitySlot> findById(int id) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(AvailabilitySlotSql.FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(dataMapper.toDomain(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read availability slot " + id, ex);
        }
    }

    @Override
    public List<AvailabilitySlot> findByStylistUserId(int stylistUserId) {
        return findByStylist(stylistUserId, AvailabilitySlotSql.FIND_BY_STYLIST_USER_ID);
    }

    @Override
    public List<AvailabilitySlot> findAvailableByStylistUserId(int stylistUserId) {
        return findByStylist(stylistUserId, AvailabilitySlotSql.FIND_AVAILABLE_BY_STYLIST_USER_ID);
    }

    @Override
    public boolean hasOverlappingSlot(int stylistUserId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(AvailabilitySlotSql.COUNT_OVERLAPPING_FOR_STYLIST)) {
            statement.setInt(1, stylistUserId);
            statement.setTimestamp(2, Timestamp.valueOf(endDateTime));
            statement.setTimestamp(3, Timestamp.valueOf(startDateTime));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to check overlapping availability slots", ex);
        }
    }

    @Override
    public AvailabilitySlot create(AvailabilitySlot slot) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(AvailabilitySlotSql.INSERT, Statement.RETURN_GENERATED_KEYS)) {
            dataMapper.bindForInsert(statement, slot);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    slot.setId(generatedKeys.getInt(1));
                }
            }
            return slot;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create availability slot", ex);
        }
    }

    @Override
    public boolean cancelAvailableSlotByIdAndStylistUserId(int id, int stylistUserId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(AvailabilitySlotSql.CANCEL_AVAILABLE_BY_ID_AND_STYLIST)) {
            statement.setInt(1, id);
            statement.setInt(2, stylistUserId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to cancel availability slot " + id, ex);
        }
    }

    private List<AvailabilitySlot> findByStylist(int stylistUserId, String sql) {
        List<AvailabilitySlot> slots = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, stylistUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    slots.add(dataMapper.toDomain(resultSet));
                }
            }
            return slots;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read availability slots for stylist " + stylistUserId, ex);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    private void ensureSchema() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(AvailabilitySlotSql.CREATE_TABLE);
            try {
                statement.executeUpdate(AvailabilitySlotSql.CREATE_INDEX_BY_STYLIST_AND_START);
            } catch (SQLException ex) {
                if (ex.getErrorCode() != 1061) {
                    throw ex;
                }
            }
            seedLegacyStatuses(connection);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize availability slots schema", ex);
        }
    }

    private void seedLegacyStatuses(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE availability_slots
                SET status = ?
                WHERE status = 0
                """)) {
            statement.setInt(1, AvailabilitySlotStatus.Available.getValue());
            statement.executeUpdate();
        }
    }
}
