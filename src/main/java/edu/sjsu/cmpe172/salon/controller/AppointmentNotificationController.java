package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.dto.AppointmentConfirmationDispatchResponse;
import edu.sjsu.cmpe172.salon.dto.AppointmentDto;
import edu.sjsu.cmpe172.salon.dto.MockNotificationResponse;
import edu.sjsu.cmpe172.salon.enums.UserRole;
import edu.sjsu.cmpe172.salon.security.SalonUserPrincipal;
import edu.sjsu.cmpe172.salon.service.AppointmentService;
import edu.sjsu.cmpe172.salon.service.NotificationGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentNotificationController {
    private final AppointmentService appointmentService;
    private final NotificationGatewayService notificationGatewayService;

    public AppointmentNotificationController(AppointmentService appointmentService,
                                             NotificationGatewayService notificationGatewayService) {
        this.appointmentService = appointmentService;
        this.notificationGatewayService = notificationGatewayService;
    }

    @PostMapping("/{appointmentId}/confirmation")
    public ResponseEntity<AppointmentConfirmationDispatchResponse> sendConfirmation(
            @PathVariable int appointmentId,
            @AuthenticationPrincipal SalonUserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required.");
        }

        AppointmentDto appointment = appointmentService.getAppointmentViewById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found."));

        if (principal.getUserRole() == UserRole.Customer && appointment.getCustomerUserId() != principal.getUserId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only send confirmations for your own appointments.");
        }

        if (principal.getUserRole() == UserRole.Stylist && appointment.getStylistUserId() != principal.getUserId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only send confirmations for your assigned appointments.");
        }

        MockNotificationResponse notificationResponse = notificationGatewayService.sendAppointmentConfirmation(appointment);

        AppointmentConfirmationDispatchResponse response = new AppointmentConfirmationDispatchResponse(
                appointment.getId(),
                notificationResponse.getNotificationId(),
                notificationResponse.getStatus(),
                notificationResponse.getProvider());

        return ResponseEntity.ok(response);
    }
}
