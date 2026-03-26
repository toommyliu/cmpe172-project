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
                availability_slot_id INT NOT NULL
            )
            """;

    public static final String FIND_ALL = """
            SELECT id, customer_user_id, stylist_user_id, service_id, availability_slot_id
            FROM appointments
            ORDER BY id
            """;

    public static final String FIND_BY_ID = """
            SELECT id, customer_user_id, stylist_user_id, service_id, availability_slot_id
            FROM appointments
            WHERE id = ?
            """;

    public static final String FIND_BY_CUSTOMER_USER_ID = """
            SELECT id, customer_user_id, stylist_user_id, service_id, availability_slot_id
            FROM appointments
            WHERE customer_user_id = ?
            ORDER BY id
            """;

    public static final String FIND_BY_STYLIST_USER_ID = """
            SELECT id, customer_user_id, stylist_user_id, service_id, availability_slot_id
            FROM appointments
            WHERE stylist_user_id = ?
            ORDER BY id
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
}
