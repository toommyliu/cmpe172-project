package edu.sjsu.cmpe172.salon.repository.sql;

public final class AppointmentSql {
    private AppointmentSql() {
    }

    public static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS appointments (
                id INT AUTO_INCREMENT PRIMARY KEY,
                customer_user_id INT NOT NULL,
                stylist_user_id INT NOT NULL,
                service_id INT NOT NULL,
                availability_slot_id INT NOT NULL,
                CONSTRAINT fk_appointments_services
                    FOREIGN KEY (service_id) REFERENCES services(id)
            )
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
                s.end_datetime AS slot_end_datetime
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
                s.end_datetime AS slot_end_datetime
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
                s.end_datetime AS slot_end_datetime
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
                s.end_datetime AS slot_end_datetime
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
            INSERT INTO appointments (customer_user_id, stylist_user_id, service_id, availability_slot_id)
            VALUES (?, ?, ?, ?)
            """;

    public static final String UPDATE = """
            UPDATE appointments
            SET customer_user_id = ?,
                stylist_user_id = ?,
                service_id = ?,
                availability_slot_id = ?
            WHERE id = ?
            """;

    public static final String DELETE_BY_ID = """
            DELETE FROM appointments
            WHERE id = ?
            """;

    public static final String FIND_BY_ID_FOR_UPDATE = """
            SELECT id, customer_user_id, stylist_user_id, service_id, availability_slot_id
            FROM appointments
            WHERE id = ?
            FOR UPDATE
            """;
}
