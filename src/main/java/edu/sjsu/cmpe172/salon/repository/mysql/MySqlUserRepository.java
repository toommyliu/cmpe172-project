package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.enums.Speciality;
import edu.sjsu.cmpe172.salon.enums.UserRole;
import edu.sjsu.cmpe172.salon.model.Customer;
import edu.sjsu.cmpe172.salon.model.Stylist;
import edu.sjsu.cmpe172.salon.model.User;
import edu.sjsu.cmpe172.salon.repository.UserRepository;
import edu.sjsu.cmpe172.salon.repository.mapper.UserDataMapper;
import edu.sjsu.cmpe172.salon.repository.sql.UserSql;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
public class MySqlUserRepository implements UserRepository {
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminFirstName;
    private final String adminLastName;
    private final UserDataMapper dataMapper;

    public MySqlUserRepository(@Value("${salon.db.url}") String dbUrl,
                               @Value("${salon.db.username}") String dbUsername,
                               @Value("${salon.db.password}") String dbPassword,
                               @Value("${salon.auth.admin.email:admin@salon.local}") String adminEmail,
                               @Value("${salon.auth.admin.password:admin12345}") String adminPassword,
                               @Value("${salon.auth.admin.first-name:Salon}") String adminFirstName,
                               @Value("${salon.auth.admin.last-name:Admin}") String adminLastName,
                               @Value("${salon.auth.admin.bootstrap-enabled:true}") boolean bootstrapAdminEnabled,
                               UserDataMapper dataMapper) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
        this.dataMapper = dataMapper;
        ensureSchema();
        seedSpecialities();
        if (bootstrapAdminEnabled) {
            ensureDefaultAdmin();
        }
    }

    @Override
    public Optional<User> findByEmailAddress(String emailAddress) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(UserSql.FIND_BY_EMAIL)) {
            statement.setString(1, emailAddress);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(dataMapper.toDomain(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read user by email", ex);
        }
    }

    @Override
    public Optional<User> findById(int userId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(UserSql.FIND_BY_ID)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(dataMapper.toDomain(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read user by id " + userId, ex);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(UserSql.FIND_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(dataMapper.toDomain(resultSet));
            }
            return users;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read users", ex);
        }
    }

    @Override
    public List<Stylist> findAllStylists() {
        List<Stylist> stylists = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(UserSql.FIND_ALL_STYLISTS);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                stylists.add((Stylist) dataMapper.toDomain(resultSet));
            }
            return stylists;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read stylists", ex);
        }
    }

    @Override
    public Customer createCustomer(Customer customer) {
        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);

            int userId;
            try (PreparedStatement statement = connection.prepareStatement(UserSql.INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
                dataMapper.bindForUserInsert(statement, customer);
                statement.executeUpdate();
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new IllegalStateException("Failed to create user for customer");
                    }
                    userId = generatedKeys.getInt(1);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(UserSql.INSERT_CUSTOMER)) {
                dataMapper.bindForCustomerInsert(statement, customer, userId);
                statement.executeUpdate();
            }

            connection.commit();
            customer.setId(userId);
            return customer;
        } catch (SQLException ex) {
            rollbackQuietly(connection);
            if (ex.getErrorCode() == 1062) {
                throw new IllegalArgumentException("Email address already in use.", ex);
            }
            throw new IllegalStateException("Failed to create customer", ex);
        } finally {
            closeQuietly(connection);
        }
    }

    @Override
    public boolean assignRole(int userId, UserRole role, Speciality speciality) {
        if (role != UserRole.Stylist) {
            throw new IllegalArgumentException("Only stylist role assignment is supported.");
        }
        if (speciality == null || speciality == Speciality.None) {
            throw new IllegalArgumentException("Speciality is required for stylist role.");
        }

        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);

            if (!exists(connection, UserSql.USER_EXISTS, userId)) {
                connection.rollback();
                return false;
            }

            if (exists(connection, UserSql.IS_ADMIN, userId)) {
                connection.rollback();
                throw new IllegalArgumentException("Cannot reassign admin users.");
            }

            try (PreparedStatement statement = connection.prepareStatement(UserSql.DELETE_CUSTOMER_ROLE)) {
                statement.setInt(1, userId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(UserSql.UPSERT_STYLIST)) {
                statement.setInt(1, userId);
                statement.setInt(2, speciality.getValue());
                statement.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException ex) {
            rollbackQuietly(connection);
            throw new IllegalStateException("Failed to assign role for user " + userId, ex);
        } finally {
            closeQuietly(connection);
        }
    }

    private boolean exists(Connection connection, String sql, int userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    private void ensureSchema() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(UserSql.CREATE_USERS_TABLE);
            statement.executeUpdate(UserSql.CREATE_SPECIALITIES_TABLE);
            statement.executeUpdate(UserSql.CREATE_CUSTOMERS_TABLE);
            statement.executeUpdate(UserSql.CREATE_STYLISTS_TABLE);
            statement.executeUpdate(UserSql.CREATE_ADMINS_TABLE);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize users schema", ex);
        }
    }

    private void ensureDefaultAdmin() {
        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);

            Integer userId = findUserIdByEmail(connection, adminEmail);
            if (userId == null) {
                try (PreparedStatement statement = connection.prepareStatement(UserSql.INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, adminFirstName);
                    statement.setString(2, adminLastName);
                    statement.setString(3, adminEmail);
                    statement.setString(4, new BCryptPasswordEncoder().encode(adminPassword));
                    statement.executeUpdate();
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new IllegalStateException("Failed to create default admin user");
                        }
                        userId = generatedKeys.getInt(1);
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(UserSql.INSERT_ADMIN)) {
                statement.setInt(1, userId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(UserSql.DELETE_CUSTOMER_ROLE)) {
                statement.setInt(1, userId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(UserSql.DELETE_STYLIST_ROLE)) {
                statement.setInt(1, userId);
                statement.executeUpdate();
            }

            connection.commit();
        } catch (SQLException ex) {
            rollbackQuietly(connection);
            throw new IllegalStateException("Failed to ensure default admin user", ex);
        } finally {
            closeQuietly(connection);
        }
    }

    private Integer findUserIdByEmail(Connection connection, String emailAddress) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UserSql.FIND_USER_ID_BY_EMAIL)) {
            statement.setString(1, emailAddress);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
                return null;
            }
        }
    }

    private void seedSpecialities() {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(UserSql.UPSERT_SPECIALITY)) {
            for (Speciality speciality : Speciality.values()) {
                if (speciality == Speciality.None) {
                    continue;
                }
                statement.setInt(1, speciality.getValue());
                statement.setString(2, speciality.name());
                statement.setString(3, speciality.toString());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to seed specialities", ex);
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
