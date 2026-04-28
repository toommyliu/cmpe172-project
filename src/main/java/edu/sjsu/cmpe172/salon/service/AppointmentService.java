package edu.sjsu.cmpe172.salon.service;

import edu.sjsu.cmpe172.salon.enums.AppointmentStatus;
import edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus;
import edu.sjsu.cmpe172.salon.dto.AppointmentDto;
import edu.sjsu.cmpe172.salon.exception.SlotReservationConflictException;
import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.model.AvailabilitySlot;
import edu.sjsu.cmpe172.salon.model.Stylist;
import edu.sjsu.cmpe172.salon.model.User;
import edu.sjsu.cmpe172.salon.repository.AppointmentRepository;
import edu.sjsu.cmpe172.salon.repository.AvailabilitySlotRepository;
import edu.sjsu.cmpe172.salon.repository.ServiceRepository;
import edu.sjsu.cmpe172.salon.repository.UserRepository;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.Duration;

@Service
public class AppointmentService {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);
    private static final int DEFAULT_PROVIDER_ID = 1;
    private static final int SLOT_RESERVATION_MAX_ATTEMPTS = 3;

    private final AppointmentRepository repository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ProviderScheduleService providerScheduleService;
    private final MonitoringService monitoringService;

    public AppointmentService(AppointmentRepository repository,
                              AvailabilitySlotRepository availabilitySlotRepository,
                              ServiceRepository serviceRepository,
                              UserRepository userRepository,
                              ProviderScheduleService providerScheduleService,
                              MonitoringService monitoringService) {
        this.repository = repository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.providerScheduleService = providerScheduleService;
        this.monitoringService = monitoringService;
    }

    public List<AppointmentDto> getAllAppointmentViews() {
        return repository.findAllViews();
    }

    public Optional<AppointmentDto> getAppointmentViewById(int id) {
        return repository.findViewById(id);
    }

    public Optional<Appointment> getAppointmentById(int id) {
        return repository.findById(id);
    }

    public List<AppointmentDto> getAppointmentViewsForCustomer(int customerUserId) {
        return repository.findViewsByCustomerUserId(customerUserId);
    }

    public List<AppointmentDto> getAppointmentViewsForStylist(int stylistUserId) {
        return repository.findViewsByStylistUserId(stylistUserId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Appointment createAppointment(Appointment appointment) {
        Timer.Sample timer = monitoringService.startAppointmentCreateTimer();
        long startedNanos = System.nanoTime();
        String metricStatus = "failed";
        String metricReason = "unknown";

        logger.info(
                "appointment_booking_started customerUserId={} stylistUserId={} serviceId={} availabilitySlotId={}",
                appointment.getCustomerUserId(),
                appointment.getStylistUserId(),
                appointment.getServiceId(),
                appointment.getAvailabilitySlotId());

        try {
            validateAppointmentRequest(appointment);

            AvailabilitySlot slot = availabilitySlotRepository.findById(appointment.getAvailabilitySlotId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected time slot does not exist."));

            if (slot.getStylistUserId() != appointment.getStylistUserId()) {
                throw new IllegalArgumentException("Selected time slot does not belong to the selected stylist.");
            }
            if (slot.getStatus() != AvailabilitySlotStatus.Available) {
                throw new IllegalArgumentException("Selected time slot is no longer available.");
            }
            if (!slot.getStartDateTime().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Selected time slot must be in the future.");
            }
            if (!providerScheduleService.isSlotWithinProviderHours(
                    DEFAULT_PROVIDER_ID,
                    slot.getStartDateTime(),
                    slot.getEndDateTime())) {
                throw new IllegalArgumentException("Selected time slot is outside provider operating hours.");
            }

            User stylistUser = userRepository.findById(appointment.getStylistUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected stylist does not exist."));
            if (!(stylistUser instanceof Stylist stylist)) {
                throw new IllegalArgumentException("Selected user is not a stylist.");
            }

            edu.sjsu.cmpe172.salon.model.Service selectedService = serviceRepository.findById(appointment.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("A valid service selection is required."));
            if (stylist.getServiceId() != appointment.getServiceId()) {
                throw new IllegalArgumentException("Selected stylist does not provide the chosen service.");
            }
            long slotDurationMinutes = Duration.between(slot.getStartDateTime(), slot.getEndDateTime()).toMinutes();
            if (slotDurationMinutes != selectedService.getDurationMinutes()) {
                throw new IllegalArgumentException("Selected slot duration does not match the service duration.");
            }

            Appointment createdAppointment = reserveSlotAndCreateAppointment(appointment);
            long durationMs = Duration.ofNanos(System.nanoTime() - startedNanos).toMillis();
            metricStatus = "success";
            metricReason = "none";
            monitoringService.recordAppointmentCreated();

            logger.info(
                    "appointment_booking_succeeded appointmentId={} customerUserId={} stylistUserId={} serviceId={} availabilitySlotId={} durationMs={}",
                    createdAppointment.getId(),
                    createdAppointment.getCustomerUserId(),
                    createdAppointment.getStylistUserId(),
                    createdAppointment.getServiceId(),
                    createdAppointment.getAvailabilitySlotId(),
                    durationMs);
            return createdAppointment;
        } catch (SlotReservationConflictException ex) {
            metricReason = "slot_conflict";
            monitoringService.recordAppointmentFailed(metricReason);
            logger.warn(
                    "appointment_booking_conflict customerUserId={} stylistUserId={} availabilitySlotId={} reason=\"slot reserved by another customer\"",
                    appointment.getCustomerUserId(),
                    appointment.getStylistUserId(),
                    appointment.getAvailabilitySlotId());
            throw ex;
        } catch (IllegalArgumentException ex) {
            metricReason = "validation";
            monitoringService.recordAppointmentFailed(metricReason);
            logger.warn(
                    "appointment_booking_rejected customerUserId={} stylistUserId={} serviceId={} availabilitySlotId={} reason=\"{}\"",
                    appointment.getCustomerUserId(),
                    appointment.getStylistUserId(),
                    appointment.getServiceId(),
                    appointment.getAvailabilitySlotId(),
                    ex.getMessage());
            throw ex;
        } catch (IllegalStateException ex) {
            metricReason = isDatabaseFailure(ex) ? "database" : "unexpected";
            monitoringService.recordAppointmentFailed(metricReason);
            logger.error(
                    "appointment_booking_failed customerUserId={} availabilitySlotId={} errorType={} message=\"{}\"",
                    appointment.getCustomerUserId(),
                    appointment.getAvailabilitySlotId(),
                    rootCause(ex).getClass().getSimpleName(),
                    ex.getMessage(),
                    ex);
            throw ex;
        } catch (RuntimeException ex) {
            metricReason = "unexpected";
            monitoringService.recordAppointmentFailed(metricReason);
            logger.error(
                    "appointment_booking_failed customerUserId={} availabilitySlotId={} errorType={} message=\"{}\"",
                    appointment.getCustomerUserId(),
                    appointment.getAvailabilitySlotId(),
                    rootCause(ex).getClass().getSimpleName(),
                    ex.getMessage(),
                    ex);
            throw ex;
        } finally {
            monitoringService.recordAppointmentCreateLatency(timer, metricStatus, metricReason);
        }
    }

    private Appointment reserveSlotAndCreateAppointment(Appointment appointment) {
        SlotReservationConflictException lastConflict = null;
        for (int attempt = 1; attempt <= SLOT_RESERVATION_MAX_ATTEMPTS; attempt++) {
            try {
                return repository.createWithSlotReservation(appointment);
            } catch (SlotReservationConflictException ex) {
                lastConflict = ex;
                if (attempt == SLOT_RESERVATION_MAX_ATTEMPTS) {
                    break;
                }

                try {
                    Thread.sleep(50L * attempt);
                } catch (InterruptedException interruptedEx) {
                    Thread.currentThread().interrupt();
                    throw new SlotReservationConflictException("Selected time slot was just booked by another customer.", interruptedEx);
                }
            }
        }
        throw new SlotReservationConflictException("Selected time slot was just booked by another customer.", lastConflict);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Appointment updateAppointment(Appointment appointment) {
        return repository.update(appointment);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean deleteAppointment(int id) {
        return repository.deleteById(id);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean cancelAppointment(int id, int userId) {
        return repository.findById(id).map(appointment -> {
            if (appointment.getCustomerUserId() != userId && appointment.getStylistUserId() != userId) {
                throw new IllegalArgumentException("You are not authorized to cancel this appointment.");
            }
            if (appointment.getStatus() == AppointmentStatus.Canceled) {
                return true; // Already canceled
            }

            appointment.setStatus(AppointmentStatus.Canceled);
            repository.update(appointment);

            // Make the slot available again
            availabilitySlotRepository.findById(appointment.getAvailabilitySlotId()).ifPresent(slot -> {
                slot.setStatus(AvailabilitySlotStatus.Available);
                availabilitySlotRepository.update(slot);
            });

            logger.info(
                    "appointment_cancelled appointmentId={} customerUserId={} stylistUserId={} availabilitySlotId={} operation=cancel status=success",
                    appointment.getId(),
                    appointment.getCustomerUserId(),
                    appointment.getStylistUserId(),
                    appointment.getAvailabilitySlotId());
            return true;
        }).orElse(false);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean completeAppointment(int id, int userId) {
        return repository.findById(id).map(appointment -> {
            if (appointment.getStylistUserId() != userId) {
                throw new IllegalArgumentException("Only the assigned stylist can mark this appointment as completed.");
            }
            if (appointment.getStatus() == AppointmentStatus.Complete) {
                return true; // Already completed
            }
            if (appointment.getStatus() == AppointmentStatus.Canceled) {
                throw new IllegalArgumentException("Cannot complete a canceled appointment.");
            }

            appointment.setStatus(AppointmentStatus.Complete);
            repository.update(appointment);
            logger.info(
                    "appointment_completed appointmentId={} customerUserId={} stylistUserId={} availabilitySlotId={} operation=complete status=success",
                    appointment.getId(),
                    appointment.getCustomerUserId(),
                    appointment.getStylistUserId(),
                    appointment.getAvailabilitySlotId());
            return true;
        }).orElse(false);
    }

    private boolean isDatabaseFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private void validateAppointmentRequest(Appointment appointment) {
        if (appointment.getCustomerUserId() <= 0) {
            throw new IllegalArgumentException("A valid customer is required.");
        }
        if (appointment.getStylistUserId() <= 0) {
            throw new IllegalArgumentException("A valid stylist is required.");
        }
        if (appointment.getAvailabilitySlotId() <= 0) {
            throw new IllegalArgumentException("Please select an available time slot.");
        }
        if (appointment.getServiceId() <= 0) {
            throw new IllegalArgumentException("Please select a service.");
        }
    }
}
