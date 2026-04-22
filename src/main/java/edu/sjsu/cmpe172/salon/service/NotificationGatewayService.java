package edu.sjsu.cmpe172.salon.service;

import edu.sjsu.cmpe172.salon.dto.AppointmentConfirmationNotificationRequest;
import edu.sjsu.cmpe172.salon.dto.AppointmentDto;
import edu.sjsu.cmpe172.salon.dto.MockNotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationGatewayService.class);

    private final RestTemplate restTemplate;
    private final String notificationBaseUrl;
    private final String notificationApiKey;

    public NotificationGatewayService(@Value("${salon.external.notification.base-url}") String notificationBaseUrl,
                                      @Value("${salon.external.notification.api-key}") String notificationApiKey) {
        this.restTemplate = new RestTemplate();
        this.notificationBaseUrl = notificationBaseUrl;
        this.notificationApiKey = notificationApiKey;
    }

    public MockNotificationResponse sendAppointmentConfirmation(AppointmentDto appointment) {
        AppointmentConfirmationNotificationRequest request = new AppointmentConfirmationNotificationRequest();
        request.setAppointmentId(appointment.getId());
        request.setCustomerUserId(appointment.getCustomerUserId());
        request.setCustomerName(appointment.getCustomerName());
        request.setStylistName(appointment.getStylistName());
        request.setServiceName(appointment.getServiceName());
        request.setAppointmentStart(appointment.getSlotStartDateTime());
        request.setAppointmentEnd(appointment.getSlotEndDateTime());
        request.setEventType("APPOINTMENT_CONFIRMED");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", notificationApiKey);

        HttpEntity<AppointmentConfirmationNotificationRequest> entity = new HttpEntity<>(request, headers);

        try {
            logger.info("Dispatching appointment confirmation. appointmentId={}", appointment.getId());

            ResponseEntity<MockNotificationResponse> response = restTemplate.postForEntity(
                    notificationBaseUrl + "/notifications/appointment-confirmations",
                    entity,
                    MockNotificationResponse.class
            );

            if (response.getBody() == null) {
                throw new IllegalStateException("Notification service returned an empty response.");
            }

            logger.info(
                    "Notification accepted. appointmentId={}, notificationId={}, status={}",
                    appointment.getId(),
                    response.getBody().getNotificationId(),
                    response.getBody().getStatus());
            return response.getBody();
        } catch (RestClientException ex) {
            logger.error("Failed to call notification service. appointmentId={}", appointment.getId(), ex);
            throw new IllegalStateException("Failed to call notification service.", ex);
        }
    }
}
