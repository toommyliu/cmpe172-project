package edu.sjsu.cmpe172.salon.repository.mapper;

import edu.sjsu.cmpe172.salon.enums.UserRole;
import edu.sjsu.cmpe172.salon.model.Admin;
import edu.sjsu.cmpe172.salon.model.Customer;
import edu.sjsu.cmpe172.salon.model.Stylist;
import edu.sjsu.cmpe172.salon.model.User;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserDataMapper {
    public User toDomain(ResultSet resultSet) throws SQLException {
        UserRole role = UserRole.fromValue(resultSet.getInt("role_value"));
        return switch (role) {
            case Admin -> toAdmin(resultSet);
            case Stylist -> toStylist(resultSet);
            case Customer -> toCustomer(resultSet);
        };
    }

    public void bindForUserInsert(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.getFirstName());
        statement.setString(2, user.getLastName());
        statement.setString(3, user.getEmailAddress());
        statement.setString(4, user.getPassword());
    }

    public void bindForCustomerInsert(PreparedStatement statement, Customer customer, int userId) throws SQLException {
        statement.setInt(1, userId);
        statement.setString(2, customer.getPhoneNumber());
    }

    private Customer toCustomer(ResultSet resultSet) throws SQLException {
        Customer customer = new Customer();
        hydrateBase(resultSet, customer);
        customer.setPhoneNumber(resultSet.getString("phone_number"));
        return customer;
    }

    private Stylist toStylist(ResultSet resultSet) throws SQLException {
        Stylist stylist = new Stylist();
        hydrateBase(resultSet, stylist);
        stylist.setServiceId(resultSet.getInt("service_id"));
        stylist.setServiceName(resultSet.getString("service_name"));
        return stylist;
    }

    private Admin toAdmin(ResultSet resultSet) throws SQLException {
        Admin admin = new Admin();
        hydrateBase(resultSet, admin);
        return admin;
    }

    private void hydrateBase(ResultSet resultSet, User user) throws SQLException {
        user.setId(resultSet.getInt("id"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmailAddress(resultSet.getString("email_address"));
        user.setPassword(resultSet.getString("password_hash"));
    }
}
