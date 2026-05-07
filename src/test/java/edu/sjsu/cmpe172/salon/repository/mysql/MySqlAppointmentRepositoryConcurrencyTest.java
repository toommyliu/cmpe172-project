package edu.sjsu.cmpe172.salon.repository.mysql;

import edu.sjsu.cmpe172.salon.enums.AppointmentStatus;
import edu.sjsu.cmpe172.salon.enums.AvailabilitySlotStatus;
import edu.sjsu.cmpe172.salon.exception.SlotReservationConflictException;
import edu.sjsu.cmpe172.salon.model.Appointment;
import edu.sjsu.cmpe172.salon.model.AvailabilitySlot;
import edu.sjsu.cmpe172.salon.repository.mapper.AppointmentDataMapper;
import edu.sjsu.cmpe172.salon.repository.mapper.AppointmentDtoDataMapper;
import edu.sjsu.cmpe172.salon.repository.mapper.AvailabilitySlotDataMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class MySqlAppointmentRepositoryConcurrencyTest {
    private static final Logger logger = LoggerFactory.getLogger(MySqlAppointmentRepositoryConcurrencyTest.class);
    private static final int MYSQL_DUPLICATE_KEY_ERROR_CODE = 1062;

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    @Test
    void createWithSlotReservationPreventsDoubleBooking_mysqlConcurrencyTest() throws Exception {
        DatabaseConfig database = mysqlDatabase();
        RepositorySet repositories = repositoriesFor(database);
        int serviceId = insertService(database);
        int stylistUserId = 20;
        AvailabilitySlot slot = repositories.slotRepository().create(futureSlot(stylistUserId));

        int customerCount = 2;
        logger.info("mysql_double_booking_concurrency_test_started customerCount={} slotId={}",
                customerCount,
                slot.getId());
        List<BookingOutcome> outcomes = reserveSameSlotConcurrently(
                repositories.appointmentRepository(),
                customerCount,
                stylistUserId,
                serviceId,
                slot.getId());

        long successfulBookings = count(outcomes, BookingOutcome.Success);
        long rejectedBookings = count(outcomes, BookingOutcome.Conflict);
        logger.info("mysql_double_booking_concurrency_test_finished slotId={} successfulBookings={} rejectedBookings={}",
                slot.getId(),
                successfulBookings,
                rejectedBookings);

        assertEquals(1, successfulBookings);
        assertEquals(customerCount - 1, rejectedBookings);
        assertEquals(1, countAppointmentsForSlotByStatus(database, slot.getId(), AppointmentStatus.Booked));
        assertNoDuplicateBookedAppointments(database, slot.getId());
        assertSlotState(database, slot.getId(), AvailabilitySlotStatus.Booked, 1);
    }

    @Test
    void activeBookingUniqueIndexAllowsHistoryButRejectsDuplicateBookedAppointments() throws Exception {
        DatabaseConfig database = mysqlDatabase();
        RepositorySet repositories = repositoriesFor(database);
        int serviceId = insertService(database);
        int stylistUserId = 30;
        AvailabilitySlot slot = repositories.slotRepository().create(futureSlot(stylistUserId));

        int firstAppointmentId = insertAppointment(
                database,
                101,
                stylistUserId,
                serviceId,
                slot.getId(),
                AppointmentStatus.Booked);

        SQLException duplicateException = assertThrows(SQLException.class, () -> insertAppointment(
                database,
                102,
                stylistUserId,
                serviceId,
                slot.getId(),
                AppointmentStatus.Booked));
        assertEquals(MYSQL_DUPLICATE_KEY_ERROR_CODE, duplicateException.getErrorCode());
        logger.info("mysql_active_booking_index_rejected_duplicate slotId={} errorCode={}",
                slot.getId(),
                duplicateException.getErrorCode());

        updateAppointmentStatus(database, firstAppointmentId, AppointmentStatus.Canceled);

        insertAppointment(
                database,
                103,
                stylistUserId,
                serviceId,
                slot.getId(),
                AppointmentStatus.Booked);

        int totalAppointments = countAppointmentsForSlot(database, slot.getId());
        int bookedAppointments = countAppointmentsForSlotByStatus(database, slot.getId(), AppointmentStatus.Booked);
        logger.info("mysql_active_booking_index_allows_rebooking_after_cancel slotId={} totalAppointments={} bookedAppointments={}",
                slot.getId(),
                totalAppointments,
                bookedAppointments);

        assertEquals(2, totalAppointments);
        assertEquals(1, bookedAppointments);
        assertNoDuplicateBookedAppointments(database, slot.getId());
    }

    private RepositorySet repositoriesFor(DatabaseConfig database) {
        MySqlAvailabilitySlotRepository slotRepository = new MySqlAvailabilitySlotRepository(
                database.url(),
                database.username(),
                database.password(),
                new AvailabilitySlotDataMapper());
        MySqlAppointmentRepository appointmentRepository = new MySqlAppointmentRepository(
                database.url(),
                database.username(),
                database.password(),
                new AppointmentDataMapper(),
                new AppointmentDtoDataMapper());
        return new RepositorySet(slotRepository, appointmentRepository);
    }

    private List<BookingOutcome> reserveSameSlotConcurrently(MySqlAppointmentRepository appointmentRepository,
            int customerCount,
            int stylistUserId,
            int serviceId,
            int availabilitySlotId) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(customerCount);
        CountDownLatch ready = new CountDownLatch(customerCount);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<BookingOutcome>> futures = new ArrayList<>();
            for (int customerUserId = 1; customerUserId <= customerCount; customerUserId++) {
                futures.add(executor.submit(bookSlotAtSameTime(
                        appointmentRepository,
                        ready,
                        start,
                        customerUserId,
                        stylistUserId,
                        serviceId,
                        availabilitySlotId)));
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS), "booking workers did not become ready");
            start.countDown();

            List<BookingOutcome> outcomes = new ArrayList<>();
            for (Future<BookingOutcome> future : futures) {
                outcomes.add(future.get(30, TimeUnit.SECONDS));
            }
            return outcomes;
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "booking workers did not stop");
        }
    }

    private Callable<BookingOutcome> bookSlotAtSameTime(MySqlAppointmentRepository appointmentRepository,
            CountDownLatch ready,
            CountDownLatch start,
            int customerUserId,
            int stylistUserId,
            int serviceId,
            int availabilitySlotId) {
        return () -> {
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
                return BookingOutcome.Success;
            } catch (SlotReservationConflictException ex) {
                return BookingOutcome.Conflict;
            }
        };
    }

    private AvailabilitySlot futureSlot(int stylistUserId) {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        return new AvailabilitySlot(
                0,
                stylistUserId,
                start,
                start.plusMinutes(30));
    }

    private int insertService(DatabaseConfig database) throws Exception {
        try (Connection connection = DriverManager.getConnection(database.url(), database.username(), database.password());
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO services (code, name, description, price, duration_minutes)
                        VALUES (?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, "haircut_" + UUID.randomUUID().toString().replace("-", ""));
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

    private int insertAppointment(DatabaseConfig database,
            int customerUserId,
            int stylistUserId,
            int serviceId,
            int availabilitySlotId,
            AppointmentStatus status) throws Exception {
        try (Connection connection = DriverManager.getConnection(database.url(), database.username(), database.password());
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO appointments (customer_user_id, stylist_user_id, service_id, availability_slot_id, status)
                        VALUES (?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, customerUserId);
            statement.setInt(2, stylistUserId);
            statement.setInt(3, serviceId);
            statement.setInt(4, availabilitySlotId);
            statement.setInt(5, status.getValue());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();
                return generatedKeys.getInt(1);
            }
        }
    }

    private void updateAppointmentStatus(DatabaseConfig database, int appointmentId, AppointmentStatus status)
            throws Exception {
        try (Connection connection = DriverManager.getConnection(database.url(), database.username(), database.password());
                PreparedStatement statement = connection.prepareStatement("""
                        UPDATE appointments
                        SET status = ?
                        WHERE id = ?
                        """)) {
            statement.setInt(1, status.getValue());
            statement.setInt(2, appointmentId);
            assertEquals(1, statement.executeUpdate());
        }
    }

    private int countAppointmentsForSlot(DatabaseConfig database, int availabilitySlotId) throws Exception {
        try (Connection connection = DriverManager.getConnection(database.url(), database.username(), database.password());
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

    private int countAppointmentsForSlotByStatus(DatabaseConfig database,
            int availabilitySlotId,
            AppointmentStatus status) throws Exception {
        try (Connection connection = DriverManager.getConnection(database.url(), database.username(), database.password());
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT COUNT(*)
                        FROM appointments
                        WHERE availability_slot_id = ?
                          AND status = ?
                        """)) {
            statement.setInt(1, availabilitySlotId);
            statement.setInt(2, status.getValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private void assertNoDuplicateBookedAppointments(DatabaseConfig database, int availabilitySlotId) throws Exception {
        try (Connection connection = DriverManager.getConnection(database.url(), database.username(), database.password());
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT COUNT(*)
                        FROM (
                            SELECT availability_slot_id
                            FROM appointments
                            WHERE availability_slot_id = ?
                              AND status = ?
                            GROUP BY availability_slot_id
                            HAVING COUNT(*) > 1
                        ) duplicate_bookings
                        """)) {
            statement.setInt(1, availabilitySlotId);
            statement.setInt(2, AppointmentStatus.Booked.getValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                assertEquals(0, resultSet.getInt(1));
            }
        }
    }

    private void assertSlotState(DatabaseConfig database,
            int slotId,
            AvailabilitySlotStatus expectedStatus,
            int expectedVersion) throws Exception {
        try (Connection connection = DriverManager.getConnection(database.url(), database.username(), database.password());
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

    private long count(List<BookingOutcome> outcomes, BookingOutcome expectedOutcome) {
        return outcomes.stream()
                .filter(outcome -> outcome == expectedOutcome)
                .count();
    }

    private DatabaseConfig mysqlDatabase() {
        return new DatabaseConfig(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private record DatabaseConfig(String url, String username, String password) {
    }

    private record RepositorySet(MySqlAvailabilitySlotRepository slotRepository,
            MySqlAppointmentRepository appointmentRepository) {
    }

    private enum BookingOutcome {
        Success,
        Conflict
    }
}
