package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MySqlAppointmentRepository implements AppointmentRepository {
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
                        throw new IllegalArgumentException("Selected time slot is no longer available.");
                    }

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
            throw new IllegalStateException("Failed to create appointment", ex);
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
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize appointments schema", ex);
        }
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
