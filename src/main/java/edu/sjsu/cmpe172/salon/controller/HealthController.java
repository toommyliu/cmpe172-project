package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.dto.HealthCheckResponse;
import edu.sjsu.cmpe172.salon.enums.OperationalStatus;
import edu.sjsu.cmpe172.salon.service.HealthCheckService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final HealthCheckService healthCheckService;

    public HealthController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthCheckResponse> health() {
        HealthCheckResponse response = healthCheckService.checkHealth();
        if (response.status() == OperationalStatus.DOWN) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
