package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.repository.AppointmentRepository;
import edu.sjsu.cmpe172.salon.repository.mapper.AppointmentDataMapper;
import edu.sjsu.cmpe172.salon.repository.sql.AppointmentSql;
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

    public MySqlAppointmentRepository(@Value("${salon.db.url}") String dbUrl,
                                      @Value("${salon.db.username}") String dbUsername,
                                      @Value("${salon.db.password}") String dbPassword,
                                      AppointmentDataMapper dataMapper) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.dataMapper = dataMapper;
        ensureSchema();
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> appointments = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(AppointmentSql.FIND_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                appointments.add(dataMapper.toDomain(resultSet));
            }
            return appointments;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read appointments", ex);
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
    public List<Appointment> findByCustomerUserId(int customerUserId) {
        return findByUserId(AppointmentSql.FIND_BY_CUSTOMER_USER_ID, customerUserId);
    }

    @Override
    public List<Appointment> findByStylistUserId(int stylistUserId) {
        return findByUserId(AppointmentSql.FIND_BY_STYLIST_USER_ID, stylistUserId);
    }

    @Override
    public Appointment create(Appointment appointment) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(AppointmentSql.INSERT, Statement.RETURN_GENERATED_KEYS)) {
            dataMapper.bindForInsert(statement, appointment);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    appointment.setId(generatedKeys.getInt(1));
                }
            }
            return appointment;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create appointment", ex);
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
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(AppointmentSql.DELETE_BY_ID)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete appointment " + id, ex);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    private List<Appointment> findByUserId(String sql, int userId) {
        List<Appointment> appointments = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    appointments.add(dataMapper.toDomain(resultSet));
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
            statement.executeUpdate(AppointmentSql.CREATE_TABLE);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize appointments schema", ex);
        }
    }
}
