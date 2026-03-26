package edu.sjsu.cmpe172.salon.service;

import edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus;
import edu.sjsu.cmpe172.salon.model.AvailabilitySlot;
import edu.sjsu.cmpe172.salon.repository.AvailabilitySlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvailabilitySlotService {
    private final AvailabilitySlotRepository repository;

    public AvailabilitySlotService(AvailabilitySlotRepository repository) {
        this.repository = repository;
    }

    public List<AvailabilitySlot> getSlotsForStylist(int stylistUserId) {
        return repository.findByStylistUserId(stylistUserId);
    }

    public List<AvailabilitySlot> getAvailableSlotsForStylist(int stylistUserId) {
        return repository.findAvailableByStylistUserId(stylistUserId);
    }

    public AvailabilitySlot createSlot(int stylistUserId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            throw new IllegalArgumentException("Start and end times are required.");
        }
        if (!startDateTime.isBefore(endDateTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
        if (!startDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time must be in the future.");
        }
        if (repository.hasOverlappingSlot(stylistUserId, startDateTime, endDateTime)) {
            throw new IllegalArgumentException("This slot overlaps with an existing slot.");
        }

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setStylistUserId(stylistUserId);
        slot.setStartDateTime(startDateTime);
        slot.setEndDateTime(endDateTime);
        slot.setStatus(AvailabilitySlotStatus.Available);
        return repository.create(slot);
    }

    public boolean cancelSlot(int slotId, int stylistUserId) {
        return repository.cancelAvailableSlotByIdAndStylistUserId(slotId, stylistUserId);
    }
}
