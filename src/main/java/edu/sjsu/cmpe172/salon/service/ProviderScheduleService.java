package edu.sjsu.cmpe172.salon.service;

import edu.sjsu.cmpe172.salon.model.AvailabilitySlot;
import edu.sjsu.cmpe172.salon.model.ProviderDateOverride;
import edu.sjsu.cmpe172.salon.model.ProviderEffectiveHours;
import edu.sjsu.cmpe172.salon.model.ProviderWeeklyHours;
import edu.sjsu.cmpe172.salon.repository.ProviderDateOverrideRepository;
import edu.sjsu.cmpe172.salon.repository.ProviderWeeklyHoursRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProviderScheduleService {
    private final ProviderWeeklyHoursRepository weeklyHoursRepository;
    private final ProviderDateOverrideRepository dateOverrideRepository;

    public ProviderScheduleService(ProviderWeeklyHoursRepository weeklyHoursRepository,
                                   ProviderDateOverrideRepository dateOverrideRepository) {
        this.weeklyHoursRepository = weeklyHoursRepository;
        this.dateOverrideRepository = dateOverrideRepository;
    }

    public Map<DayOfWeek, ProviderWeeklyHours> getWeeklyHoursByDay(int providerId) {
        validateProviderId(providerId);
        Map<DayOfWeek, ProviderWeeklyHours> byDay = new EnumMap<>(DayOfWeek.class);
        for (ProviderWeeklyHours hours : weeklyHoursRepository.findByProviderId(providerId)) {
            byDay.put(hours.getDayOfWeek(), hours);
        }

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            byDay.computeIfAbsent(dayOfWeek, day -> {
                ProviderWeeklyHours defaultHours = new ProviderWeeklyHours();
                defaultHours.setProviderId(providerId);
                defaultHours.setDayOfWeek(day);
                defaultHours.setClosed(true);
                return defaultHours;
            });
        }
        return byDay;
    }

    public List<ProviderDateOverride> getDateOverrides(int providerId) {
        validateProviderId(providerId);
        List<ProviderDateOverride> overrides = new ArrayList<>(dateOverrideRepository.findByProviderId(providerId));
        overrides.sort(Comparator.comparing(ProviderDateOverride::getOverrideDate));
        return overrides;
    }

    public void upsertWeeklyHours(int providerId,
                                  DayOfWeek dayOfWeek,
                                  boolean closed,
                                  LocalTime openTime,
                                  LocalTime closeTime) {
        validateProviderId(providerId);
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("Day of week is required.");
        }

        ProviderWeeklyHours hours = new ProviderWeeklyHours();
        hours.setProviderId(providerId);
        hours.setDayOfWeek(dayOfWeek);
        hours.setClosed(closed);

        if (closed) {
            hours.setOpenTime(null);
            hours.setCloseTime(null);
        } else {
            validateOpenCloseTimes(openTime, closeTime);
            hours.setOpenTime(openTime);
            hours.setCloseTime(closeTime);
        }

        weeklyHoursRepository.upsert(hours);
    }

    public ProviderDateOverride upsertDateOverride(int providerId,
                                                   LocalDate overrideDate,
                                                   boolean closed,
                                                   LocalTime openTime,
                                                   LocalTime closeTime) {
        validateProviderId(providerId);
        if (overrideDate == null) {
            throw new IllegalArgumentException("Override date is required.");
        }

        ProviderDateOverride override = new ProviderDateOverride();
        override.setProviderId(providerId);
        override.setOverrideDate(overrideDate);
        override.setClosed(closed);

        if (closed) {
            override.setOpenTime(null);
            override.setCloseTime(null);
        } else {
            validateOpenCloseTimes(openTime, closeTime);
            override.setOpenTime(openTime);
            override.setCloseTime(closeTime);
        }

        return dateOverrideRepository.upsert(override);
    }

    public boolean deleteDateOverride(int overrideId, int providerId) {
        validateProviderId(providerId);
        if (overrideId <= 0) {
            throw new IllegalArgumentException("A valid override id is required.");
        }
        return dateOverrideRepository.deleteByIdAndProviderId(overrideId, providerId);
    }

    public ProviderEffectiveHours resolveEffectiveHours(int providerId, LocalDate date) {
        validateProviderId(providerId);
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }

        Optional<ProviderDateOverride> dateOverride = dateOverrideRepository.findByProviderIdAndDate(providerId, date);
        if (dateOverride.isPresent()) {
            ProviderDateOverride override = dateOverride.get();
            if (override.isClosed()) {
                return ProviderEffectiveHours.closedHours();
            }
            if (override.getOpenTime() == null || override.getCloseTime() == null) {
                return ProviderEffectiveHours.closedHours();
            }
            return ProviderEffectiveHours.openHours(override.getOpenTime(), override.getCloseTime());
        }

        ProviderWeeklyHours weeklyHours = getWeeklyHoursByDay(providerId).get(date.getDayOfWeek());
        if (weeklyHours == null || weeklyHours.isClosed()) {
            return ProviderEffectiveHours.closedHours();
        }
        if (weeklyHours.getOpenTime() == null || weeklyHours.getCloseTime() == null) {
            return ProviderEffectiveHours.closedHours();
        }
        return ProviderEffectiveHours.openHours(weeklyHours.getOpenTime(), weeklyHours.getCloseTime());
    }

    public boolean isSlotWithinProviderHours(int providerId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        validateProviderId(providerId);
        if (startDateTime == null || endDateTime == null || !startDateTime.isBefore(endDateTime)) {
            return false;
        }
        if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
            return false;
        }

        ProviderEffectiveHours effectiveHours = resolveEffectiveHours(providerId, startDateTime.toLocalDate());
        if (effectiveHours.closed()) {
            return false;
        }

        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = endDateTime.toLocalTime();
        return !startTime.isBefore(effectiveHours.openTime())
                && !endTime.isAfter(effectiveHours.closeTime());
    }

    public List<AvailabilitySlot> filterSlotsWithinProviderHours(int providerId, List<AvailabilitySlot> slots) {
        if (slots == null || slots.isEmpty()) {
            return List.of();
        }

        return slots.stream()
                .filter(slot -> isSlotWithinProviderHours(providerId, slot.getStartDateTime(), slot.getEndDateTime()))
                .toList();
    }

    private void validateOpenCloseTimes(LocalTime openTime, LocalTime closeTime) {
        if (openTime == null || closeTime == null) {
            throw new IllegalArgumentException("Open and close time are required.");
        }
        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("Close time must be after open time.");
        }
    }

    private void validateProviderId(int providerId) {
        if (providerId <= 0) {
            throw new IllegalArgumentException("A valid provider is required.");
        }
    }
}
