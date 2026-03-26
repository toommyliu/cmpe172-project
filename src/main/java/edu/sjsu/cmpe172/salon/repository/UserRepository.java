package edu.sjsu.cmpe172.salon.repository;

import edu.sjsu.cmpe172.salon.enums.UserRole;
import edu.sjsu.cmpe172.salon.model.Customer;
import edu.sjsu.cmpe172.salon.model.Stylist;
import edu.sjsu.cmpe172.salon.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmailAddress(String emailAddress);

    Optional<User> findById(int userId);

    List<User> findAll();

    Customer createCustomer(Customer customer);

    List<Stylist> findAllStylists();

    boolean assignRole(int userId, UserRole role, int serviceId);
}
