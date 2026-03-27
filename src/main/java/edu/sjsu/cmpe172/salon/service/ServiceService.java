package edu.sjsu.cmpe172.salon.service;

import edu.sjsu.cmpe172.salon.model.Service;
import edu.sjsu.cmpe172.salon.repository.ServiceRepository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceService {
    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<Service> findAll() {
        return serviceRepository.findAll();
    }

    public Optional<Service> findById(int id) {
        return serviceRepository.findById(id);
    }

    public void save(Service service) {
        serviceRepository.save(service);
    }

    public void deleteById(int id) {
        serviceRepository.deleteById(id);
    }
}
