package edu.sjsu.cmpe172.salon.repository.sql;

public final class ProviderDateOverrideSql {
    private ProviderDateOverrideSql() {
    }

    public static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS provider_date_overrides (
                id INT AUTO_INCREMENT PRIMARY KEY,
                provider_id INT NOT NULL,
                override_date DATE NOT NULL,
                is_closed BOOLEAN NOT NULL,
                open_time TIME,
                close_time TIME,
                UNIQUE KEY uq_provider_override_date (provider_id, override_date)
            )
            """;

    public static final String FIND_BY_PROVIDER_ID = """
            SELECT id, provider_id, override_date, is_closed, open_time, close_time
            FROM provider_date_overrides
            WHERE provider_id = ?
            ORDER BY override_date
            """;

    public static final String FIND_BY_PROVIDER_ID_AND_DATE = """
            SELECT id, provider_id, override_date, is_closed, open_time, close_time
            FROM provider_date_overrides
            WHERE provider_id = ?
              AND override_date = ?
            """;

    public static final String UPSERT = """
            INSERT INTO provider_date_overrides (provider_id, override_date, is_closed, open_time, close_time)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                is_closed = VALUES(is_closed),
                open_time = VALUES(open_time),
                close_time = VALUES(close_time)
            """;

    public static final String DELETE_BY_ID_AND_PROVIDER_ID = """
            DELETE FROM provider_date_overrides
            WHERE id = ?
              AND provider_id = ?
            """;
}
