package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.model.ProviderDateOverride;
import edu.sjsu.cmpe172.salon.repository.ProviderDateOverrideRepository;
import edu.sjsu.cmpe172.salon.repository.mapper.ProviderDateOverrideDataMapper;
import edu.sjsu.cmpe172.salon.repository.sql.ProviderDateOverrideSql;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MySqlProviderDateOverrideRepository implements ProviderDateOverrideRepository {
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final ProviderDateOverrideDataMapper dataMapper;

    public MySqlProviderDateOverrideRepository(@Value("${salon.db.url}") String dbUrl,
                                               @Value("${salon.db.username}") String dbUsername,
                                               @Value("${salon.db.password}") String dbPassword,
                                               ProviderDateOverrideDataMapper dataMapper) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.dataMapper = dataMapper;
        ensureSchema();
    }

    @Override
    public List<ProviderDateOverride> findByProviderId(int providerId) {
        List<ProviderDateOverride> overrides = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ProviderDateOverrideSql.FIND_BY_PROVIDER_ID)) {
            statement.setInt(1, providerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    overrides.add(dataMapper.toDomain(resultSet));
                }
            }
            return overrides;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read provider date overrides", ex);
        }
    }

    @Override
    public Optional<ProviderDateOverride> findByProviderIdAndDate(int providerId, LocalDate date) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ProviderDateOverrideSql.FIND_BY_PROVIDER_ID_AND_DATE)) {
            statement.setInt(1, providerId);
            statement.setDate(2, Date.valueOf(date));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(dataMapper.toDomain(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read provider date override", ex);
        }
    }

    @Override
    public ProviderDateOverride upsert(ProviderDateOverride override) {
        Integer existingId = findOverrideIdByProviderAndDate(override.getProviderId(), override.getOverrideDate());
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ProviderDateOverrideSql.UPSERT, Statement.RETURN_GENERATED_KEYS)) {
            dataMapper.bindForUpsert(statement, override);
            statement.executeUpdate();
            if (existingId != null) {
                override.setId(existingId);
                return override;
            }
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    override.setId(generatedKeys.getInt(1));
                }
            }
            return override;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save provider date override", ex);
        }
    }

    @Override
    public boolean deleteByIdAndProviderId(int id, int providerId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ProviderDateOverrideSql.DELETE_BY_ID_AND_PROVIDER_ID)) {
            statement.setInt(1, id);
            statement.setInt(2, providerId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete provider date override", ex);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    private Integer findOverrideIdByProviderAndDate(int providerId, LocalDate date) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(ProviderDateOverrideSql.FIND_BY_PROVIDER_ID_AND_DATE)) {
            statement.setInt(1, providerId);
            statement.setDate(2, Date.valueOf(date));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to resolve provider date override id", ex);
        }
    }

    private void ensureSchema() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(ProviderDateOverrideSql.CREATE_TABLE);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize provider date overrides schema", ex);
        }
    }
}
