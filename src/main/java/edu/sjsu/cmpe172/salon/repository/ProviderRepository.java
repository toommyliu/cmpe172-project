package edu.sjsu.cmpe172.salon.repository;

import edu.sjsu.cmpe172.salon.model.Provider;

import java.util.Optional;

public interface ProviderRepository {
    Optional<Provider> findById(int id);

    Provider upsert(Provider provider);
}
