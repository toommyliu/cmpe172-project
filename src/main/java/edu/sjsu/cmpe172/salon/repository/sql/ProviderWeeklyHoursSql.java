package edu.sjsu.cmpe172.salon.repository.sql;

public final class ProviderWeeklyHoursSql {
    private ProviderWeeklyHoursSql() {
    }

    public static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS provider_weekly_hours (
                provider_id INT NOT NULL,
                day_of_week TINYINT NOT NULL,
                is_closed BOOLEAN NOT NULL,
                open_time TIME,
                close_time TIME,
                PRIMARY KEY (provider_id, day_of_week)
            )
            """;

    public static final String FIND_BY_PROVIDER_ID = """
            SELECT provider_id, day_of_week, is_closed, open_time, close_time
            FROM provider_weekly_hours
            WHERE provider_id = ?
            ORDER BY day_of_week
            """;

    public static final String UPSERT = """
            INSERT INTO provider_weekly_hours (provider_id, day_of_week, is_closed, open_time, close_time)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                is_closed = VALUES(is_closed),
                open_time = VALUES(open_time),
                close_time = VALUES(close_time)
            """;

    public static final String COUNT_BY_PROVIDER_ID = """
            SELECT COUNT(*)
            FROM provider_weekly_hours
            WHERE provider_id = ?
            """;
}
