package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus;
import edu.sjsu.cmpe172.salon.enums.AppointmentStatus;
import edu.sjsu.cmpe172.salon.dto.AppointmentDto;
import edu.sjsu.cmpe172.salon.exception.SlotReservationConflictException;
import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.repository.AppointmentRepository;
import edu.sjsu.cmpe172.salon.repository.mapper.AppointmentDataMapper;
import edu.sjsu.cmpe172.salon.repository.mapper.AppointmentDtoDataMapper;
import edu.sjsu.cmpe172.salon.repository.sql.AppointmentSql;
import edu.sjsu.cmpe172.salon.repository.sql.AvailabilitySlotSql;
import edu.sjsu.cmpe172.salon.repository.sql.ServiceSql;
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
public class MySqlAppointmentRepository implements AppointmentRepository {
    private static final int MYSQL_DUPLICATE_KEY_ERROR_CODE = 1062;
    private static final String MYSQL_DATABASE_PRODUCT = "mysql";

    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final AppointmentDataMapper dataMapper;
    private final AppointmentDtoDataMapper dtoDataMapper;

    public MySqlAppointmentRepository(@Value("${salon.db.url}") String dbUrl,
                                      @Value("${salon.db.username}") String dbUsername,
                                      @Value("${salon.db.password}") String dbPassword,
                                      AppointmentDataMapper dataMapper,
                                      AppointmentDtoDataMapper dtoDataMapper) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.dataMapper = dataMapper;
        this.dtoDataMapper = dtoDataMapper;
        ensureSchema();
    }

    @Override
    public List<AppointmentDto> findAllViews() {
        List<AppointmentDto> appointments = new ArrayList<>();
        try (Connection connection = openConnection();
            PreparedStatement statement = connection.prepareStatement(AppointmentSql.FIND_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                appointments.add(dtoDataMapper.toDomain(resultSet));
            }
            return appointments;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read appointments", ex);
        }
    }

    @Override
    public Optional<AppointmentDto> findViewById(int id) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(AppointmentSql.FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(dtoDataMapper.toDomain(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read appointment view " + id, ex);
        }
    }

    @Override
    public Optional<Appointment> findById(int id) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(AppointmentSql.FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(dataMapper.toDomain(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read appointment " + id, ex);
        }
    }

    @Override
    public List<AppointmentDto> findViewsByCustomerUserId(int customerUserId) {
        return findViewsByUserId(AppointmentSql.FIND_BY_CUSTOMER_USER_ID, customerUserId);
    }

    @Override
    public List<AppointmentDto> findViewsByStylistUserId(int stylistUserId) {
        return findViewsByUserId(AppointmentSql.FIND_BY_STYLIST_USER_ID, stylistUserId);
    }

    @Override
    public Appointment create(Appointment appointment) {
        return createWithSlotReservation(appointment);
    }

    @Override
    public Appointment createWithSlotReservation(Appointment appointment) {
        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            try (PreparedStatement findSlotStatement = connection.prepareStatement(AvailabilitySlotSql.FIND_BY_ID);
                 PreparedStatement markSlotBookedStatement = connection.prepareStatement(AvailabilitySlotSql.MARK_SLOT_BOOKED_BY_ID_AND_VERSION);
                 PreparedStatement insertAppointmentStatement = connection.prepareStatement(AppointmentSql.INSERT, Statement.RETURN_GENERATED_KEYS)) {

                findSlotStatement.setInt(1, appointment.getAvailabilitySlotId());
                try (ResultSet resultSet = findSlotStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new IllegalArgumentException("Selected time slot does not exist.");
                    }

                    int stylistUserId = resultSet.getInt("stylist_user_id");
                    AvailabilitySlotStatus slotStatus = AvailabilitySlotStatus.fromValue(resultSet.getInt("status"));
                    int slotVersion = resultSet.getInt("version");
                    if (stylistUserId != appointment.getStylistUserId()) {
                        throw new IllegalArgumentException("Selected time slot does not belong to the selected stylist.");
                    }
                    if (slotStatus != AvailabilitySlotStatus.Available) {
                        if (slotStatus == AvailabilitySlotStatus.Booked) {
                            throw new SlotReservationConflictException("Selected time slot was just booked by another customer.");
                        }
                        throw new IllegalArgumentException("Selected time slot is no longer available.");
                    }

                    // Optimistic slot reservation: only the transaction holding
                    // the latest available version may flip the slot to Booked.
                    markSlotBookedStatement.setInt(1, appointment.getAvailabilitySlotId());
                    markSlotBookedStatement.setInt(2, slotVersion);
                    if (markSlotBookedStatement.executeUpdate() == 0) {
                        throw new SlotReservationConflictException("Selected time slot was just booked by another customer.");
                    }
                }

                dataMapper.bindForInsert(insertAppointmentStatement, appointment);
                insertAppointmentStatement.executeUpdate();
                try (ResultSet generatedKeys = insertAppointmentStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        appointment.setId(generatedKeys.getInt(1));
                    }
                }
            }

            connection.commit();
            return appointment;
        } catch (IllegalArgumentException ex) {
            rollbackQuietly(connection);
            throw ex;
        } catch (SQLException ex) {
            rollbackQuietly(connection);
            if (isActiveBookingDuplicateKey(ex)) {
                throw new SlotReservationConflictException("Selected time slot was just booked by another customer.", ex);
            }
            throw new IllegalStateException("Failed to create appointment", ex);
        } finally {
            closeQuietly(connection);
        }
    }

    @Override
    public Appointment rescheduleWithSlotReservation(Appointment appointment) {
        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            int oldAvailabilitySlotId;
            try (PreparedStatement findAppointmentStatement = connection.prepareStatement(AppointmentSql.FIND_BY_ID_FOR_UPDATE)) {
                findAppointmentStatement.setInt(1, appointment.getId());
                try (ResultSet resultSet = findAppointmentStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new IllegalArgumentException("Appointment not found.");
                    }
                    AppointmentStatus currentStatus = AppointmentStatus.fromValue(resultSet.getInt("status"));
                    if (currentStatus != AppointmentStatus.Booked) {
                        throw new IllegalArgumentException("Only booked appointments can be rescheduled.");
                    }
                    oldAvailabilitySlotId = resultSet.getInt("availability_slot_id");
                }
            }

            try (PreparedStatement findSlotStatement = connection.prepareStatement(AvailabilitySlotSql.FIND_BY_ID);
                 PreparedStatement markSlotBookedStatement = connection.prepareStatement(AvailabilitySlotSql.MARK_SLOT_BOOKED_BY_ID_AND_VERSION);
                 PreparedStatement updateAppointmentStatement = connection.prepareStatement(AppointmentSql.UPDATE);
                 PreparedStatement setOldSlotAvailableStatement = connection.prepareStatement(AvailabilitySlotSql.MARK_SLOT_AVAILABLE_BY_ID)) {

                findSlotStatement.setInt(1, appointment.getAvailabilitySlotId());
                try (ResultSet resultSet = findSlotStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new IllegalArgumentException("Selected time slot does not exist.");
                    }

                    int stylistUserId = resultSet.getInt("stylist_user_id");
                    AvailabilitySlotStatus slotStatus = AvailabilitySlotStatus.fromValue(resultSet.getInt("status"));
                    int slotVersion = resultSet.getInt("version");
                    if (stylistUserId != appointment.getStylistUserId()) {
                        throw new IllegalArgumentException("Selected time slot does not belong to the selected stylist.");
                    }
                    if (slotStatus != AvailabilitySlotStatus.Available) {
                        if (slotStatus == AvailabilitySlotStatus.Booked) {
                            throw new SlotReservationConflictException("Selected time slot was just booked by another customer.");
                        }
                        throw new IllegalArgumentException("Selected time slot is no longer available.");
                    }

                    // Optimistic slot reservation mirrors appointment creation
                    // so concurrent reschedules cannot claim the same slot.
                    markSlotBookedStatement.setInt(1, appointment.getAvailabilitySlotId());
                    markSlotBookedStatement.setInt(2, slotVersion);
                    if (markSlotBookedStatement.executeUpdate() == 0) {
                        throw new SlotReservationConflictException("Selected time slot was just booked by another customer.");
                    }
                }

                dataMapper.bindForUpdate(updateAppointmentStatement, appointment);
                if (updateAppointmentStatement.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Appointment not found: " + appointment.getId());
                }

                setOldSlotAvailableStatement.setInt(1, oldAvailabilitySlotId);
                setOldSlotAvailableStatement.executeUpdate();
            }

            connection.commit();
            return appointment;
        } catch (IllegalArgumentException ex) {
            rollbackQuietly(connection);
            throw ex;
        } catch (SQLException ex) {
            rollbackQuietly(connection);
            if (isActiveBookingDuplicateKey(ex)) {
                throw new SlotReservationConflictException("Selected time slot was just booked by another customer.", ex);
            }
            throw new IllegalStateException("Failed to reschedule appointment " + appointment.getId(), ex);
        } finally {
            closeQuietly(connection);
        }
    }

    @Override
    public Appointment update(Appointment appointment) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(AppointmentSql.UPDATE)) {
            dataMapper.bindForUpdate(statement, appointment);
            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new IllegalArgumentException("Appointment not found: " + appointment.getId());
            }
            return appointment;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update appointment " + appointment.getId(), ex);
        }
    }

    @Override
    public int completeBookedAppointmentsForCustomerEndedBefore(int customerUserId, LocalDateTime cutoff) {
        return completeBookedAppointmentsEndedBefore(
                AppointmentSql.COMPLETE_BOOKED_FOR_CUSTOMER_ENDED_BEFORE,
                customerUserId,
                cutoff,
                "customer " + customerUserId);
    }

    @Override
    public int completeBookedAppointmentsForStylistEndedBefore(int stylistUserId, LocalDateTime cutoff) {
        return completeBookedAppointmentsEndedBefore(
                AppointmentSql.COMPLETE_BOOKED_FOR_STYLIST_ENDED_BEFORE,
                stylistUserId,
                cutoff,
                "stylist " + stylistUserId);
    }

    @Override
    public boolean deleteById(int id) {
        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);

            Integer slotId = null;
            try (PreparedStatement findStatement = connection.prepareStatement(AppointmentSql.FIND_BY_ID_FOR_UPDATE)) {
                findStatement.setInt(1, id);
                try (ResultSet resultSet = findStatement.executeQuery()) {
                    if (resultSet.next()) {
                        slotId = resultSet.getInt("availability_slot_id");
                    } else {
                        connection.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement deleteStatement = connection.prepareStatement(AppointmentSql.DELETE_BY_ID)) {
                deleteStatement.setInt(1, id);
                if (deleteStatement.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
            }

            if (slotId != null) {
                try (PreparedStatement setAvailableStatement = connection.prepareStatement(AvailabilitySlotSql.MARK_SLOT_AVAILABLE_BY_ID)) {
                    setAvailableStatement.setInt(1, slotId);
                    setAvailableStatement.executeUpdate();
                }
            }

            connection.commit();
            return true;
        } catch (SQLException ex) {
            rollbackQuietly(connection);
            throw new IllegalStateException("Failed to delete appointment " + id, ex);
        } finally {
            closeQuietly(connection);
        }
    }

    private int completeBookedAppointmentsEndedBefore(String sql, int userId, LocalDateTime cutoff, String scopeDescription) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // The caller chooses customer or stylist scope by passing the matching SQL statement.
            statement.setInt(1, AppointmentStatus.Complete.getValue());
            statement.setInt(2, userId);
            statement.setInt(3, AppointmentStatus.Booked.getValue());
            statement.setTimestamp(4, Timestamp.valueOf(cutoff));
            return statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to complete ended appointments for " + scopeDescription, ex);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    private List<AppointmentDto> findViewsByUserId(String sql, int userId) {
        List<AppointmentDto> appointments = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    appointments.add(dtoDataMapper.toDomain(resultSet));
                }
                return appointments;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read appointments for user " + userId, ex);
        }
    }

    private void ensureSchema() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(ServiceSql.CREATE_TABLE);
            statement.executeUpdate(AppointmentSql.CREATE_TABLE);
            ensureActiveBookingConstraint(connection, statement);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize appointments schema", ex);
        }
    }

    private void ensureActiveBookingConstraint(Connection connection, Statement statement) throws SQLException {
        if (!isMySql(connection)) {
            throw new SQLException("Appointments schema requires MySQL generated columns and unique indexes.");
        }

        // Existing databases may predate the active-booking guard, so install
        // it idempotently without hiding corrupt duplicate booked rows.
        if (!columnExists(connection, "appointments", AppointmentSql.ACTIVE_BOOKING_SLOT_COLUMN)) {
            statement.executeUpdate(AppointmentSql.ADD_ACTIVE_BOOKING_SLOT_COLUMN);
        }
        if (!indexExists(connection, "appointments", AppointmentSql.ACTIVE_BOOKING_SLOT_INDEX)) {
            statement.executeUpdate(AppointmentSql.CREATE_ACTIVE_BOOKING_SLOT_INDEX);
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String schemaName = connection.getCatalog();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = ?
                  AND table_name = ?
                  AND column_name = ?
                """)) {
            statement.setString(1, schemaName);
            statement.setString(2, tableName);
            statement.setString(3, columnName);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    private boolean indexExists(Connection connection, String tableName, String indexName) throws SQLException {
        String schemaName = connection.getCatalog();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COUNT(*)
                FROM information_schema.statistics
                WHERE table_schema = ?
                  AND table_name = ?
                  AND index_name = ?
                """)) {
            statement.setString(1, schemaName);
            statement.setString(2, tableName);
            statement.setString(3, indexName);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    private boolean isActiveBookingDuplicateKey(SQLException ex) {
        SQLException current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (current.getErrorCode() == MYSQL_DUPLICATE_KEY_ERROR_CODE
                    && message != null
                    && message.contains(AppointmentSql.ACTIVE_BOOKING_SLOT_INDEX)) {
                return true;
            }
            current = current.getNextException();
        }
        return false;
    }

    private boolean isMySql(Connection connection) throws SQLException {
        String databaseProductName = connection.getMetaData().getDatabaseProductName();
        if (databaseProductName == null) {
            return false;
        }
        return databaseProductName.toLowerCase().contains(MYSQL_DATABASE_PRODUCT);
    }

    private void rollbackQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
