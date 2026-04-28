package edu.sjsu.cmpe172.salon.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class MonitoringService {
    private final MeterRegistry meterRegistry;

    public MonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startAppointmentCreateTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordAppointmentCreateLatency(Timer.Sample sample, String status, String reason) {
        sample.stop(Timer.builder("appointments.create.latency")
                .description("Latency of appointment creation requests")
                .tag("status", status)
                .tag("reason", reason)
                .register(meterRegistry));
    }

    public void recordAppointmentCreated() {
        Counter.builder("appointments.created.count")
                .description("Successfully created appointments")
                .register(meterRegistry)
                .increment();
    }

    public void recordAppointmentFailed(String reason) {
        Counter.builder("appointments.failed.count")
                .description("Failed appointment creation attempts")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void recordNotificationFailed() {
        Counter.builder("notifications.failed.count")
                .description("Failed appointment confirmation notifications")
                .register(meterRegistry)
                .increment();
    }
}
