package edu.sjsu.cmpe172.salon.dto;

import edu.sjsu.cmpe172.salon.enums.OperationalStatus;

import java.time.Instant;

public record HealthCheckResponse(
        OperationalStatus status,
        String application,
        OperationalStatus database,
        OperationalStatus notificationGateway,
        Instant timestamp) {
}
