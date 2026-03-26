package edu.sjsu.cmpe172.salon.model;

import java.time.LocalTime;

public record ProviderEffectiveHours(boolean closed, LocalTime openTime, LocalTime closeTime) {
    public static ProviderEffectiveHours closedHours() {
        return new ProviderEffectiveHours(true, null, null);
    }

    public static ProviderEffectiveHours openHours(LocalTime openTime, LocalTime closeTime) {
        return new ProviderEffectiveHours(false, openTime, closeTime);
    }

    /**
     * Checks if the given time falls within the provider's effective hours.
     * @param time the time to check
     * @return true if the time is within the effective hours, false otherwise
     */
    public boolean contains(LocalTime time) {
        if (closed || openTime == null || closeTime == null || time == null) {
            return false;
        }
        return !time.isBefore(openTime) && time.isBefore(closeTime);
    }
}
