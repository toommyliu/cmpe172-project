package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.model.Service;
import edu.sjsu.cmpe172.salon.repository.ServiceRepository;
import edu.sjsu.cmpe172.salon.repository.mapper.ServiceDataMapper;
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
public class MySqlServiceRepository implements ServiceRepository {
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final ServiceDataMapper dataMapper;

    public MySqlServiceRepository(@Value("${salon.db.url}") String dbUrl,
                                  @Value("${salon.db.username}") String dbUsername,
                                  @Value("${salon.db.password}") String dbPassword,
                                  ServiceDataMapper dataMapper) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.dataMapper = dataMapper;
        ensureSchema();
    }

    @Override
    public List<Service> findAll() {
        List<Service> services = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ServiceSql.FIND_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                services.add(dataMapper.toDomain(resultSet));
            }
            return services;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read services", ex);
        }
    }

    @Override
    public Optional<Service> findById(int serviceId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ServiceSql.FIND_BY_ID)) {
            statement.setInt(1, serviceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(dataMapper.toDomain(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read service by id " + serviceId, ex);
        }
    }

    @Override
    public boolean existsById(int serviceId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ServiceSql.EXISTS_BY_ID)) {
            statement.setInt(1, serviceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to check service existence", ex);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    private void ensureSchema() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(ServiceSql.CREATE_TABLE);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize services schema", ex);
        }
    }
}
