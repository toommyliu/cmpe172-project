package edu.sjsu.cmpe172.salon.repository.sql;

public final class AvailabilitySlotSql {
    private AvailabilitySlotSql() {
    }

    public static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS availability_slots (
                id INT AUTO_INCREMENT PRIMARY KEY,
                stylist_user_id INT NOT NULL,
                start_datetime DATETIME NOT NULL,
                end_datetime DATETIME NOT NULL,
                status INT NOT NULL,
                version INT NOT NULL DEFAULT 0
            )
            """;

    public static final String CREATE_INDEX_BY_STYLIST_AND_START = """
            CREATE INDEX idx_availability_slots_stylist_start
            ON availability_slots (stylist_user_id, start_datetime)
            """;

    public static final String FIND_BY_ID = """
            SELECT id, stylist_user_id, start_datetime, end_datetime, status, version
            FROM availability_slots
            WHERE id = ?
            """;

    public static final String FIND_BY_STYLIST_USER_ID = """
            SELECT id, stylist_user_id, start_datetime, end_datetime, status, version
            FROM availability_slots
            WHERE stylist_user_id = ?
            ORDER BY start_datetime
            """;

    public static final String FIND_AVAILABLE_BY_STYLIST_USER_ID = """
            SELECT id, stylist_user_id, start_datetime, end_datetime, status, version
            FROM availability_slots
            WHERE stylist_user_id = ?
              AND status = 1
              AND start_datetime > NOW()
            ORDER BY start_datetime
            """;

    public static final String COUNT_OVERLAPPING_FOR_STYLIST = """
            SELECT COUNT(*)
            FROM availability_slots
            WHERE stylist_user_id = ?
              AND status <> 100
              AND start_datetime < ?
              AND end_datetime > ?
            """;

    public static final String INSERT = """
            INSERT INTO availability_slots (stylist_user_id, start_datetime, end_datetime, status, version)
            VALUES (?, ?, ?, ?, ?)
            """;

    public static final String CANCEL_AVAILABLE_BY_ID_AND_STYLIST = """
            UPDATE availability_slots
            SET status = 100,
                version = version + 1
            WHERE id = ?
              AND stylist_user_id = ?
              AND status = 1
            """;

    public static final String MARK_SLOT_BOOKED_BY_ID_AND_VERSION = """
            UPDATE availability_slots
            SET status = 2,
                version = version + 1
            WHERE id = ?
              AND status = 1
              AND version = ?
            """;

    public static final String MARK_SLOT_AVAILABLE_BY_ID = """
            UPDATE availability_slots
            SET status = 1,
                version = version + 1
            WHERE id = ?
            """;

    public static final String UPDATE = """
            UPDATE availability_slots
            SET stylist_user_id = ?,
                start_datetime = ?,
                end_datetime = ?,
                status = ?,
                version = ?
            WHERE id = ?
            """;
}
