package edu.sjsu.cmpe172.salon.repository.sql;

public final class ServiceSql {
    private ServiceSql() {
    }

    public static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS services (
                id INT PRIMARY KEY AUTO_INCREMENT,
                code VARCHAR(50) NOT NULL UNIQUE,
                name VARCHAR(120) NOT NULL,
                description VARCHAR(255) NULL,
                price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                duration_minutes INT NOT NULL
            )
            """;

    public static final String FIND_ALL = """
            SELECT id, code, name, description, price, duration_minutes
            FROM services
            ORDER BY id
            """;

    public static final String FIND_BY_ID = """
            SELECT id, code, name, description, price, duration_minutes
            FROM services
            WHERE id = ?
            """;

    public static final String EXISTS_BY_ID = """
            SELECT 1
            FROM services
            WHERE id = ?
            """;

    public static final String INSERT_SERVICE = """
            INSERT INTO services (code, name, description, price, duration_minutes)
            VALUES (?, ?, ?, ?, ?)
            """;

    public static final String UPDATE_SERVICE = """
            UPDATE services
            SET code = ?, name = ?, description = ?, price = ?, duration_minutes = ?
            WHERE id = ?
            """;

    public static final String UPSERT_SERVICE = """
            INSERT INTO services (id, code, name, description, price, duration_minutes)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                code = VALUES(code),
                name = VALUES(name),
                description = VALUES(description),
                price = VALUES(price),
                duration_minutes = VALUES(duration_minutes)
            """;

    public static final String COUNT_ALL = """
            SELECT COUNT(*)
            FROM services
            """;

    public static final String DELETE_BY_ID = """
            DELETE FROM services
            WHERE id = ?
            """;
}
