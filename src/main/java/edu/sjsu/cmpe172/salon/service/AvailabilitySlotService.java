package edu.sjsu.cmpe172.salon.service;

import edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus;
import edu.sjsu.cmpe172.salon.model.AvailabilitySlot;
import edu.sjsu.cmpe172.salon.repository.AvailabilitySlotRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

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

    public BulkCreateResult createBulkSlotsForStylist(int stylistUserId,
                                                      LocalDate startDate,
                                                      LocalDate endDate,
                                                      Set<DayOfWeek> weekdays,
                                                      LocalTime dayStartTime,
                                                      LocalTime dayEndTime,
                                                      int slotDurationMinutes) {
        if (startDate == null || endDate == null || dayStartTime == null || dayEndTime == null) {
            throw new IllegalArgumentException("Date and time inputs are required.");
        }
        if (weekdays == null || weekdays.isEmpty()) {
            throw new IllegalArgumentException("Select at least one weekday.");
        }
        if (slotDurationMinutes <= 0) {
            throw new IllegalArgumentException("Service duration must be greater than 0 minutes.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be on or before end date.");
        }
        if (!dayStartTime.isBefore(dayEndTime)) {
            throw new IllegalArgumentException("Daily start time must be before daily end time.");
        }

        LocalDateTime now = LocalDateTime.now();
        int createdCount = 0;
        int skippedCount = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (!weekdays.contains(date.getDayOfWeek())) {
                continue;
            }

            LocalDateTime slotStart = LocalDateTime.of(date, dayStartTime);
            LocalDateTime dayEndDateTime = LocalDateTime.of(date, dayEndTime);
            while (!slotStart.plusMinutes(slotDurationMinutes).isAfter(dayEndDateTime)) {
                LocalDateTime slotEnd = slotStart.plusMinutes(slotDurationMinutes);
                if (!slotStart.isAfter(now)
                        || repository.hasOverlappingSlot(stylistUserId, slotStart, slotEnd)) {
                    skippedCount++;
                    slotStart = slotEnd;
                    continue;
                }

                AvailabilitySlot slot = new AvailabilitySlot();
                slot.setStylistUserId(stylistUserId);
                slot.setStartDateTime(slotStart);
                slot.setEndDateTime(slotEnd);
                slot.setStatus(AvailabilitySlotStatus.Available);
                repository.create(slot);
                createdCount++;
                slotStart = slotEnd;
            }
        }

        return new BulkCreateResult(createdCount, skippedCount);
    }

    public boolean cancelSlot(int slotId, int stylistUserId) {
        return repository.cancelAvailableSlotByIdAndStylistUserId(slotId, stylistUserId);
    }

    public record BulkCreateResult(int createdCount, int skippedCount) {
    }
}
