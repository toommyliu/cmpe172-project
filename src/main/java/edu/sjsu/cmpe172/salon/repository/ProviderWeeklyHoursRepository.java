package edu.sjsu.cmpe172.salon.repository;

import edu.sjsu.cmpe172.salon.model.ProviderWeeklyHours;

import java.util.List;

public interface ProviderWeeklyHoursRepository {
    List<ProviderWeeklyHours> findByProviderId(int providerId);

    void upsert(ProviderWeeklyHours hours);
}
