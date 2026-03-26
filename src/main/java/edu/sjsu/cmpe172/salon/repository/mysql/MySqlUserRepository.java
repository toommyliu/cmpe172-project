package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.enums.UserRole;
import edu.sjsu.cmpe172.salon.model.Customer;
import edu.sjsu.cmpe172.salon.model.Stylist;
import edu.sjsu.cmpe172.salon.model.User;
import edu.sjsu.cmpe172.salon.repository.UserRepository;
import edu.sjsu.cmpe172.salon.repository.mapper.UserDataMapper;
import edu.sjsu.cmpe172.salon.repository.sql.ServiceSql;
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
    private final String customerEmail;
    private final String customerPassword;
    private final String customerFirstName;
    private final String customerLastName;
    private final String stylistEmail;
    private final String stylistPassword;
    private final String stylistFirstName;
    private final String stylistLastName;
    private final UserDataMapper dataMapper;

    public MySqlUserRepository(@Value("${salon.db.url}") String dbUrl,
                               @Value("${salon.db.username}") String dbUsername,
                               @Value("${salon.db.password}") String dbPassword,
                               @Value("${salon.auth.admin.email:admin@salon.local}") String adminEmail,
                               @Value("${salon.auth.admin.password:admin12345}") String adminPassword,
                               @Value("${salon.auth.admin.first-name:Salon}") String adminFirstName,
                               @Value("${salon.auth.admin.last-name:Admin}") String adminLastName,
                               @Value("${salon.auth.customer.email:customer@salon.local}") String customerEmail,
                               @Value("${salon.auth.customer.password:customer12345}") String customerPassword,
                               @Value("${salon.auth.customer.first-name:Customer}") String customerFirstName,
                               @Value("${salon.auth.customer.last-name:User}") String customerLastName,
                               @Value("${salon.auth.stylist.email:stylist@salon.local}") String stylistEmail,
                               @Value("${salon.auth.stylist.password:stylist12345}") String stylistPassword,
                               @Value("${salon.auth.stylist.first-name:Stylist}") String stylistFirstName,
                               @Value("${salon.auth.stylist.last-name:User}") String stylistLastName,
                               @Value("${salon.auth.bootstrap-enabled:true}") boolean bootstrapEnabled,
                               UserDataMapper dataMapper) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
        this.customerEmail = customerEmail;
        this.customerPassword = customerPassword;
        this.customerFirstName = customerFirstName;
        this.customerLastName = customerLastName;
        this.stylistEmail = stylistEmail;
        this.stylistPassword = stylistPassword;
        this.stylistFirstName = stylistFirstName;
        this.stylistLastName = stylistLastName;
        this.dataMapper = dataMapper;
        ensureSchema();
        if (bootstrapEnabled) {
            ensureDefaultAdmin();
            ensureDefaultCustomer();
            ensureDefaultStylist();
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
    public boolean assignRole(int userId, UserRole role, int serviceId) {
        if (role != UserRole.Stylist) {
            throw new IllegalArgumentException("Only stylist role assignment is supported.");
        }
        if (serviceId <= 0) {
            throw new IllegalArgumentException("Service is required for stylist role.");
        }

        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);

            if (!exists(connection, UserSql.SERVICE_EXISTS, serviceId)) {
                connection.rollback();
                throw new IllegalArgumentException("Selected service does not exist.");
            }

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
                statement.setInt(2, serviceId);
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
            statement.executeUpdate(ServiceSql.CREATE_TABLE);
            seedDefaultServices(connection);
            statement.executeUpdate(UserSql.CREATE_CUSTOMERS_TABLE);
            statement.executeUpdate(UserSql.CREATE_STYLISTS_TABLE);
            statement.executeUpdate(UserSql.CREATE_ADMINS_TABLE);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize users schema", ex);
        }
    }

    private void seedDefaultServices(Connection connection) throws SQLException {
        try (PreparedStatement countStatement = connection.prepareStatement(ServiceSql.COUNT_ALL);
             ResultSet countResult = countStatement.executeQuery()) {
            if (countResult.next() && countResult.getInt(1) > 0) {
                return;
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(ServiceSql.UPSERT_SERVICE)) {
            bindService(statement, 1, "coloring", "Coloring", "Hair coloring services.", 120.00, 90);
            bindService(statement, 2, "cutting", "Cutting", "Hair cutting services.", 70.00, 60);
            bindService(statement, 3, "extensions", "Extensions", "Hair extension services.", 220.00, 120);
            bindService(statement, 4, "chemical_treatments", "Chemical Treatments", "Smoothing, perms, and related treatments.", 180.00, 120);
            bindService(statement, 5, "styling", "Styling", "Styling and blowout services.", 60.00, 45);
            bindService(statement, 6, "barbering", "Barbering", "Barbering and grooming services.", 55.00, 30);
            statement.executeBatch();
        }
    }

    private void bindService(PreparedStatement statement,
                             int id,
                             String code,
                             String name,
                             String description,
                             double price,
                             int durationMinutes) throws SQLException {
        statement.setInt(1, id);
        statement.setString(2, code);
        statement.setString(3, name);
        statement.setString(4, description);
        statement.setDouble(5, price);
        statement.setInt(6, durationMinutes);
        statement.addBatch();
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

    private void ensureDefaultCustomer() {
        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);

            Integer userId = findUserIdByEmail(connection, customerEmail);
            if (userId == null) {
                try (PreparedStatement statement = connection.prepareStatement(UserSql.INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, customerFirstName);
                    statement.setString(2, customerLastName);
                    statement.setString(3, customerEmail);
                    statement.setString(4, new BCryptPasswordEncoder().encode(customerPassword));
                    statement.executeUpdate();
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new IllegalStateException("Failed to create default customer user");
                        }
                        userId = generatedKeys.getInt(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(UserSql.INSERT_CUSTOMER)) {
                    statement.setInt(1, userId);
                    statement.setString(2, null);
                    statement.executeUpdate();
                }
            }

            connection.commit();
        } catch (SQLException ex) {
            rollbackQuietly(connection);
            throw new IllegalStateException("Failed to ensure default customer user", ex);
        } finally {
            closeQuietly(connection);
        }
    }

    private void ensureDefaultStylist() {
        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);

            Integer userId = findUserIdByEmail(connection, stylistEmail);
            if (userId == null) {
                try (PreparedStatement statement = connection.prepareStatement(UserSql.INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, stylistFirstName);
                    statement.setString(2, stylistLastName);
                    statement.setString(3, stylistEmail);
                    statement.setString(4, new BCryptPasswordEncoder().encode(stylistPassword));
                    statement.executeUpdate();
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new IllegalStateException("Failed to create default stylist user");
                        }
                        userId = generatedKeys.getInt(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(UserSql.UPSERT_STYLIST)) {
                    statement.setInt(1, userId);
                    statement.setInt(2, 1);
                    statement.executeUpdate();
                }
            }

            connection.commit();
        } catch (SQLException ex) {
            rollbackQuietly(connection);
            throw new IllegalStateException("Failed to ensure default stylist user", ex);
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
