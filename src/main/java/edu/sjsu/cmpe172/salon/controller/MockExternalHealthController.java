package edu.sjsu.cmpe172.salon.controller;

import edu.sjsu.cmpe172.salon.enums.OperationalStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockExternalHealthController {
    @GetMapping("/mock-external/health")
    public ResponseEntity<OperationalStatus> health() {
        return ResponseEntity.ok(OperationalStatus.UP);
    }
}
