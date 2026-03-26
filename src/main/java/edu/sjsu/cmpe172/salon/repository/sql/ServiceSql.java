package edu.sjsu.cmpe172.salon.repository.sql;

public final class ServiceSql {
    private ServiceSql() {
    }

    public static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS services (
                id INT PRIMARY KEY,
                code VARCHAR(50) NOT NULL UNIQUE,
                name VARCHAR(120) NOT NULL,
                description VARCHAR(255) NULL,
                price DECIMAL(10,2) NOT NULL DEFAULT 0.00
            )
            """;

    public static final String FIND_ALL = """
            SELECT id, code, name, description, price
            FROM services
            ORDER BY id
            """;

    public static final String FIND_BY_ID = """
            SELECT id, code, name, description, price
            FROM services
            WHERE id = ?
            """;

    public static final String EXISTS_BY_ID = """
            SELECT 1
            FROM services
            WHERE id = ?
            """;

    public static final String UPSERT_SERVICE = """
            INSERT INTO services (id, code, name, description, price)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                code = VALUES(code),
                name = VALUES(name),
                description = VALUES(description),
                price = VALUES(price)
            """;

    public static final String COUNT_ALL = """
            SELECT COUNT(*)
            FROM services
            """;
}
