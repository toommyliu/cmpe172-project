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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
public class NotificationGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationGatewayService.class);
    private static final Duration NOTIFICATION_TIMEOUT = Duration.ofSeconds(2);

    private final RestTemplate restTemplate;
    private final String notificationBaseUrl;
    private final String notificationApiKey;
    private final MonitoringService monitoringService;

    public NotificationGatewayService(@Value("${salon.external.notification.base-url}") String notificationBaseUrl,
                                      @Value("${salon.external.notification.api-key}") String notificationApiKey,
                                      MonitoringService monitoringService) {
        this.restTemplate = new RestTemplate(notificationRequestFactory());
        this.notificationBaseUrl = notificationBaseUrl;
        this.notificationApiKey = notificationApiKey;
        this.monitoringService = monitoringService;
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
            logger.info("notification_dispatch_started appointmentId={} customerUserId={} eventType={}",
                    appointment.getId(),
                    appointment.getCustomerUserId(),
                    request.getEventType());

            ResponseEntity<MockNotificationResponse> response = restTemplate.postForEntity(
                    notificationBaseUrl + "/notifications/appointment-confirmations",
                    entity,
                    MockNotificationResponse.class
            );

            if (response.getBody() == null) {
                throw new IllegalStateException("Notification service returned an empty response.");
            }

            logger.info(
                    "notification_accepted appointmentId={} notificationId={} status={}",
                    appointment.getId(),
                    response.getBody().getNotificationId(),
                    response.getBody().getStatus());
            return response.getBody();
        } catch (RestClientException ex) {
            monitoringService.recordNotificationFailed();
            logger.error(
                    "notification_dispatch_failed appointmentId={} errorType={} message=\"{}\"",
                    appointment.getId(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex);
            throw new IllegalStateException("Failed to call notification service.", ex);
        } catch (IllegalStateException ex) {
            monitoringService.recordNotificationFailed();
            logger.error(
                    "notification_dispatch_failed appointmentId={} errorType={} message=\"{}\"",
                    appointment.getId(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }

    public boolean isHealthy() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(notificationBaseUrl + "/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException ex) {
            logger.error(
                    "health_check_dependency_failed dependency=notificationGateway status=DOWN errorType={} message=\"{}\"",
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
            return false;
        }
    }

    private SimpleClientHttpRequestFactory notificationRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(NOTIFICATION_TIMEOUT);
        requestFactory.setReadTimeout(NOTIFICATION_TIMEOUT);
        return requestFactory;
    }
}
