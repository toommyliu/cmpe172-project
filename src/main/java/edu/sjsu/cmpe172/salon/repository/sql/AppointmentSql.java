package edu.sjsu.cmpe172.salon.repository.sql;

public final class AppointmentSql {
    private AppointmentSql() {
    }

    /*
     * active_booking_slot_id is generated from availability_slot_id only while an
     * appointment is Booked. We cannot make availability_slot_id itself unique
     * because canceled/completed appointment rows are retained as history and
     * still point at the original slot. The generated expression returns NULL
     * for those historical rows, and MySQL permits multiple NULL values in a
     * unique index. That lets the database reject duplicate active bookings
     * without blocking valid history or rebooking after cancellation.
     */
    public static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS appointments (
                id INT AUTO_INCREMENT PRIMARY KEY,
                customer_user_id INT NOT NULL,
                stylist_user_id INT NOT NULL,
                service_id INT NOT NULL,
                availability_slot_id INT NOT NULL,
                status INT NOT NULL DEFAULT 1,
                active_booking_slot_id INT
                    GENERATED ALWAYS AS (
                        CASE WHEN status = 1 THEN availability_slot_id ELSE NULL END
                    ) STORED,
                CONSTRAINT fk_appointments_services
                    FOREIGN KEY (service_id) REFERENCES services(id)
            )
            """;

    public static final String ACTIVE_BOOKING_SLOT_COLUMN = "active_booking_slot_id";

    public static final String ACTIVE_BOOKING_SLOT_INDEX = "ux_appointments_active_booking_slot";

    public static final String ADD_ACTIVE_BOOKING_SLOT_COLUMN = """
            ALTER TABLE appointments
            ADD COLUMN active_booking_slot_id INT
                GENERATED ALWAYS AS (
                    CASE WHEN status = 1 THEN availability_slot_id ELSE NULL END
                ) STORED
            """;

    public static final String CREATE_ACTIVE_BOOKING_SLOT_INDEX = """
            CREATE UNIQUE INDEX ux_appointments_active_booking_slot
            ON appointments (active_booking_slot_id)
            """;

    public static final String FIND_ALL = """
            SELECT
                a.id,
                a.customer_user_id,
                a.stylist_user_id,
                a.service_id,
                svc.name AS service_name,
                a.availability_slot_id,
                CONCAT(cu.first_name, ' ', cu.last_name) AS customer_name,
                CONCAT(su.first_name, ' ', su.last_name) AS stylist_name,
                s.start_datetime AS slot_start_datetime,
                s.end_datetime AS slot_end_datetime,
                a.status
            FROM appointments a
            INNER JOIN users cu ON cu.id = a.customer_user_id
            INNER JOIN users su ON su.id = a.stylist_user_id
            INNER JOIN services svc ON svc.id = a.service_id
            LEFT JOIN availability_slots s ON s.id = a.availability_slot_id
            ORDER BY a.id
            """;

    public static final String FIND_BY_ID = """
            SELECT
                a.id,
                a.customer_user_id,
                a.stylist_user_id,
                a.service_id,
                svc.name AS service_name,
                a.availability_slot_id,
                CONCAT(cu.first_name, ' ', cu.last_name) AS customer_name,
                CONCAT(su.first_name, ' ', su.last_name) AS stylist_name,
                s.start_datetime AS slot_start_datetime,
                s.end_datetime AS slot_end_datetime,
                a.status
            FROM appointments a
            INNER JOIN users cu ON cu.id = a.customer_user_id
            INNER JOIN users su ON su.id = a.stylist_user_id
            INNER JOIN services svc ON svc.id = a.service_id
            LEFT JOIN availability_slots s ON s.id = a.availability_slot_id
            WHERE a.id = ?
            """;

    public static final String FIND_BY_CUSTOMER_USER_ID = """
            SELECT
                a.id,
                a.customer_user_id,
                a.stylist_user_id,
                a.service_id,
                svc.name AS service_name,
                a.availability_slot_id,
                CONCAT(cu.first_name, ' ', cu.last_name) AS customer_name,
                CONCAT(su.first_name, ' ', su.last_name) AS stylist_name,
                s.start_datetime AS slot_start_datetime,
                s.end_datetime AS slot_end_datetime,
                a.status
            FROM appointments a
            INNER JOIN users cu ON cu.id = a.customer_user_id
            INNER JOIN users su ON su.id = a.stylist_user_id
            INNER JOIN services svc ON svc.id = a.service_id
            LEFT JOIN availability_slots s ON s.id = a.availability_slot_id
            WHERE a.customer_user_id = ?
            ORDER BY a.id
            """;

    public static final String FIND_BY_STYLIST_USER_ID = """
            SELECT
                a.id,
                a.customer_user_id,
                a.stylist_user_id,
                a.service_id,
                svc.name AS service_name,
                a.availability_slot_id,
                CONCAT(cu.first_name, ' ', cu.last_name) AS customer_name,
                CONCAT(su.first_name, ' ', su.last_name) AS stylist_name,
                s.start_datetime AS slot_start_datetime,
                s.end_datetime AS slot_end_datetime,
                a.status
            FROM appointments a
            INNER JOIN users cu ON cu.id = a.customer_user_id
            INNER JOIN users su ON su.id = a.stylist_user_id
            INNER JOIN services svc ON svc.id = a.service_id
            LEFT JOIN availability_slots s ON s.id = a.availability_slot_id
            WHERE a.stylist_user_id = ?
            ORDER BY a.id
            """;

    public static final String SERVICE_EXISTS = """
            SELECT 1
            FROM services
            WHERE id = ?
            """;

    public static final String INSERT = """
            INSERT INTO appointments (customer_user_id, stylist_user_id, service_id, availability_slot_id, status)
            VALUES (?, ?, ?, ?, ?)
            """;

    public static final String UPDATE = """
            UPDATE appointments
            SET customer_user_id = ?,
                stylist_user_id = ?,
                service_id = ?,
                availability_slot_id = ?,
                status = ?
            WHERE id = ?
            """;

    public static final String DELETE_BY_ID = """
            DELETE FROM appointments
            WHERE id = ?
            """;

    public static final String COMPLETE_BOOKED_FOR_CUSTOMER_ENDED_BEFORE = """
            UPDATE appointments a
            INNER JOIN availability_slots s ON s.id = a.availability_slot_id
            SET a.status = ?
            WHERE a.customer_user_id = ?
                AND a.status = ?
                AND s.end_datetime <= ?
            """;

    public static final String COMPLETE_BOOKED_FOR_STYLIST_ENDED_BEFORE = """
            UPDATE appointments a
            INNER JOIN availability_slots s ON s.id = a.availability_slot_id
            SET a.status = ?
            WHERE a.stylist_user_id = ?
                AND a.status = ?
                AND s.end_datetime <= ?
            """;

    public static final String FIND_BY_ID_FOR_UPDATE = """
            SELECT id, customer_user_id, stylist_user_id, service_id, availability_slot_id, status
            FROM appointments
            WHERE id = ?
            FOR UPDATE
            """;
}
