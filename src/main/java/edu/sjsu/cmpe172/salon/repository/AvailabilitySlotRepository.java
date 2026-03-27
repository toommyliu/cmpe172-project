package edu.sjsu.cmpe172.salon.repository;

import edu.sjsu.cmpe172.salon.model.AvailabilitySlot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilitySlotRepository {
    Optional<AvailabilitySlot> findById(int id);

    List<AvailabilitySlot> findByStylistUserId(int stylistUserId);

    List<AvailabilitySlot> findAvailableByStylistUserId(int stylistUserId);

    boolean hasOverlappingSlot(int stylistUserId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    AvailabilitySlot create(AvailabilitySlot slot);

    boolean cancelAvailableSlotByIdAndStylistUserId(int id, int stylistUserId);

    AvailabilitySlot update(AvailabilitySlot slot);
}
