package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.dto.AppointmentConfirmationNotificationRequest;
import edu.sjsu.cmpe172.salon.dto.MockNotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/mock-external/notifications")
public class MockNotificationController {
    private static final Logger logger = LoggerFactory.getLogger(MockNotificationController.class);

    private final String apiKey;

    public MockNotificationController(@Value("${salon.external.notification.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @PostMapping("/appointment-confirmations")
    public ResponseEntity<MockNotificationResponse> sendAppointmentConfirmation(
            @RequestBody AppointmentConfirmationNotificationRequest request,
            @RequestHeader(value = "X-Api-Key", required = false) String apiKeyHeader) {
        if (apiKeyHeader == null || !apiKey.equals(apiKeyHeader)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid inter-service API key.");
        }

        logger.info("Mock notification request received. appointmentId={}, customerUserId={}, eventType={}",
                request.getAppointmentId(),
                request.getCustomerUserId(),
                request.getEventType());

        MockNotificationResponse response = new MockNotificationResponse(
                UUID.randomUUID().toString(),
                "ACCEPTED",
                "mock-notification-service",
                LocalDateTime.now(),
                "Confirmation queued for " + request.getCustomerName() + "."
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
