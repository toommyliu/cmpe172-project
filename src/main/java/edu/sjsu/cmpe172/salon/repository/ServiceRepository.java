package edu.sjsu.cmpe172.salon.repository;

import edu.sjsu.cmpe172.salon.model.Service;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository {
    List<Service> findAll();

    Optional<Service> findById(int serviceId);

    boolean existsById(int serviceId);
    
    void save(Service service);

    void deleteById(int serviceId);
}
