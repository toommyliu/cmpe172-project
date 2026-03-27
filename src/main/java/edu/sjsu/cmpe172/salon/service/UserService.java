package edu.sjsu.cmpe172.salon.service;

import edu.sjsu.cmpe172.salon.dto.StylistDto;
import edu.sjsu.cmpe172.salon.enums.UserRole;
import edu.sjsu.cmpe172.salon.model.Customer;
import edu.sjsu.cmpe172.salon.model.Stylist;
import edu.sjsu.cmpe172.salon.model.User;
import edu.sjsu.cmpe172.salon.repository.ServiceRepository;
import edu.sjsu.cmpe172.salon.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ServiceRepository serviceRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<StylistDto> getAllStylistDtos() {
        return userRepository.findAllStylists().stream()
                .map(stylist -> {
                    StylistDto dto = new StylistDto();
                    dto.setId(stylist.getId());
                    dto.setFirstName(stylist.getFirstName());
                    dto.setLastName(stylist.getLastName());
                    dto.setEmailAddress(stylist.getEmailAddress());
                    dto.setServiceId(stylist.getServiceId());
                    serviceRepository.findById(stylist.getServiceId()).ifPresent(service -> {
                        dto.setServiceName(service.getName());
                        dto.setServiceDurationMinutes(service.getDurationMinutes());
                    });
                    return dto;
                }).collect(Collectors.toList());
    }

    public Customer registerCustomer(String firstName, String lastName, String emailAddress,
                                     String rawPassword, String phoneNumber) {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmailAddress(emailAddress);
        customer.setPassword(passwordEncoder.encode(rawPassword));
        customer.setPhoneNumber(phoneNumber);
        return userRepository.createCustomer(customer);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Stylist> getAllStylists() {
        return userRepository.findAllStylists();
    }

    public boolean assignStylistRole(int userId, int serviceId) {
        return userRepository.assignRole(userId, UserRole.Stylist, serviceId);
    }
}
