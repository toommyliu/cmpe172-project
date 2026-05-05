package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.enums.AppointmentStatus;
import edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus;
import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.model.AvailabilitySlot;
import edu.sjsu.cmpe172.salon.repository.mapper.AppointmentDataMapper;
import edu.sjsu.cmpe172.salon.repository.mapper.AppointmentDtoDataMapper;
import edu.sjsu.cmpe172.salon.repository.mapper.AvailabilitySlotDataMapper;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Verifies that concurrent attempts to reserve the same availability slot
// result in exactly one appointment and one rejected reservation.
class MySqlAppointmentRepositoryConcurrencyTest {
    private static final String DB_USERNAME = "sa";
    private static final String DB_PASSWORD = "";

    @Test
    void createWithSlotReservationPreventsDoubleBooking() throws Exception {
        // Use a fresh in-memory database so each run starts with an isolated schema.
        String dbUrl = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        MySqlAvailabilitySlotRepository slotRepository = new MySqlAvailabilitySlotRepository(
                dbUrl,
                DB_USERNAME,
                DB_PASSWORD,
                new AvailabilitySlotDataMapper());
        MySqlAppointmentRepository appointmentRepository = new MySqlAppointmentRepository(
                dbUrl,
                DB_USERNAME,
                DB_PASSWORD,
                new AppointmentDataMapper(),
                new AppointmentDtoDataMapper());

        int serviceId = insertService(dbUrl);
        int stylistUserId = 10;
        // Seed one available slot that both simulated customers will try to book.
        AvailabilitySlot slot = slotRepository.create(new AvailabilitySlot(
                0,
                stylistUserId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusMinutes(30)));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        // Coordinate both worker threads so they submit the reservation at the same
        // time.
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> customerA = executor.submit(bookSlotAtSameTime(
                    appointmentRepository,
                    ready,
                    start,
                    1,
                    stylistUserId,
                    serviceId,
                    slot.getId()));
            Future<Boolean> customerB = executor.submit(bookSlotAtSameTime(
                    appointmentRepository,
                    ready,
                    start,
                    2,
                    stylistUserId,
                    serviceId,
                    slot.getId()));

            // Wait until both customers are ready, then release them together.
            ready.await();
            start.countDown();

            long successfulBookings = List.of(customerA.get(), customerB.get())
                    .stream()
                    .filter(Boolean::booleanValue)
                    .count();

            // The booking invariant: one success, one appointment row, and the slot marked
            // booked.
            assertEquals(1, successfulBookings);
            assertEquals(1, countAppointmentsForSlot(dbUrl, slot.getId()));
            assertSlotState(dbUrl, slot.getId(), AvailabilitySlotStatus.Booked, 1);
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<Boolean> bookSlotAtSameTime(MySqlAppointmentRepository appointmentRepository,
            CountDownLatch ready,
            CountDownLatch start,
            int customerUserId,
            int stylistUserId,
            int serviceId,
            int availabilitySlotId) {
        return () -> {
            // Signal readiness, then block until the test releases both customers.
            ready.countDown();
            start.await();

            Appointment appointment = new Appointment(
                    0,
                    customerUserId,
                    stylistUserId,
                    serviceId,
                    availabilitySlotId,
                    AppointmentStatus.Booked);
            try {
                appointmentRepository.createWithSlotReservation(appointment);
                return true;
            } catch (IllegalArgumentException ex) {
                // Reservation conflicts are expected for the losing customer.
                return false;
            }
        };
    }

    private int insertService(String dbUrl) throws Exception {
        try (Connection connection = DriverManager.getConnection(dbUrl, DB_USERNAME, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO services (code, name, description, price, duration_minutes)
                        VALUES (?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, "haircut");
            statement.setString(2, "Haircut");
            statement.setString(3, "Test haircut");
            statement.setBigDecimal(4, java.math.BigDecimal.valueOf(50));
            statement.setInt(5, 30);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();
                return generatedKeys.getInt(1);
            }
        }
    }

    private int countAppointmentsForSlot(String dbUrl, int availabilitySlotId) throws Exception {
        try (Connection connection = DriverManager.getConnection(dbUrl, DB_USERNAME, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT COUNT(*)
                        FROM appointments
                        WHERE availability_slot_id = ?
                        """)) {
            statement.setInt(1, availabilitySlotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private void assertSlotState(String dbUrl,
            int slotId,
            AvailabilitySlotStatus expectedStatus,
            int expectedVersion) throws Exception {
        try (Connection connection = DriverManager.getConnection(dbUrl, DB_USERNAME, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT status, version
                        FROM availability_slots
                        WHERE id = ?
                        """)) {
            statement.setInt(1, slotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                assertEquals(expectedStatus.getValue(), resultSet.getInt("status"));
                assertEquals(expectedVersion, resultSet.getInt("version"));
            }
        }
    }
}
