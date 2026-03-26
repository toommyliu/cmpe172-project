package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.model.Provider;
import edu.sjsu.cmpe172.salon.repository.ProviderRepository;
import edu.sjsu.cmpe172.salon.repository.mapper.ProviderDataMapper;
import edu.sjsu.cmpe172.salon.repository.sql.ProviderSql;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class MySqlProviderRepository implements ProviderRepository {
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final ProviderDataMapper dataMapper;

    public MySqlProviderRepository(@Value("${salon.db.url}") String dbUrl,
                                   @Value("${salon.db.username}") String dbUsername,
                                   @Value("${salon.db.password}") String dbPassword,
                                   ProviderDataMapper dataMapper) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.dataMapper = dataMapper;
        ensureSchema();
    }

    @Override
    public Optional<Provider> findById(int id) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ProviderSql.FIND_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(dataMapper.toDomain(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read provider by id " + id, ex);
        }
    }

    @Override
    public Provider upsert(Provider provider) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ProviderSql.UPSERT)) {
            dataMapper.bindForUpsert(statement, provider);
            statement.executeUpdate();
            return provider;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save provider", ex);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    private void ensureSchema() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(ProviderSql.CREATE_TABLE);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize providers schema", ex);
        }
    }
}
