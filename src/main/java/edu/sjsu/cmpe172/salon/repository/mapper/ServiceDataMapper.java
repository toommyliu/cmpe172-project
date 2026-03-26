package edu.sjsu.cmpe172.salon.repository.mapper;

import edu.sjsu.cmpe172.salon.model.Service;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ServiceDataMapper {
    public Service toDomain(ResultSet resultSet) throws SQLException {
        Service service = new Service();
        service.setId(resultSet.getInt("id"));
        service.setCode(resultSet.getString("code"));
        service.setName(resultSet.getString("name"));
        service.setDescription(resultSet.getString("description"));
        service.setPrice(resultSet.getDouble("price"));
        service.setDurationMinutes(resultSet.getInt("duration_minutes"));
        return service;
    }

    public void bindForUpsert(PreparedStatement statement, Service service) throws SQLException {
        statement.setInt(1, service.getId());
        statement.setString(2, service.getCode());
        statement.setString(3, service.getName());
        statement.setString(4, service.getDescription());
        statement.setDouble(5, service.getPrice());
        statement.setInt(6, service.getDurationMinutes());
    }
}
