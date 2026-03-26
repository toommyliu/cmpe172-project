package edu.sjsu.cmpe172.salon.repository.mapper;

import edu.sjsu.cmpe172.salon.model.Provider;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ProviderDataMapper {
    public Provider toDomain(ResultSet resultSet) throws SQLException {
        Provider provider = new Provider();
        provider.setId(resultSet.getInt("id"));
        provider.setName(resultSet.getString("name"));
        provider.setAddress(resultSet.getString("address"));
        provider.setPhoneNumber(resultSet.getString("phone_number"));
        provider.setEmailAddress(resultSet.getString("email_address"));
        provider.setOpenTime(resultSet.getTimestamp("open_time"));
        provider.setCloseTime(resultSet.getTimestamp("close_time"));
        return provider;
    }

    public void bindForUpsert(PreparedStatement statement, Provider provider) throws SQLException {
        statement.setInt(1, provider.getId());
        statement.setString(2, provider.getName());
        statement.setString(3, provider.getAddress());
        statement.setString(4, provider.getPhoneNumber());
        statement.setString(5, provider.getEmailAddress());
        statement.setTimestamp(6, provider.getOpenTime());
        statement.setTimestamp(7, provider.getCloseTime());
    }
}
