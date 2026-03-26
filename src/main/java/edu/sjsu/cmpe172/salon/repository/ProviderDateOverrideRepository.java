package edu.sjsu.cmpe172.salon.repository;

import edu.sjsu.cmpe172.salon.model.ProviderDateOverride;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProviderDateOverrideRepository {
    List<ProviderDateOverride> findByProviderId(int providerId);

    Optional<ProviderDateOverride> findByProviderIdAndDate(int providerId, LocalDate date);

    ProviderDateOverride upsert(ProviderDateOverride override);

    boolean deleteByIdAndProviderId(int id, int providerId);
}
