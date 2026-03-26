package edu.sjsu.cmpe172.salon.repository.sql;

public final class ProviderSql {
    private ProviderSql() {
    }

    public static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS providers (
                id INT PRIMARY KEY,
                name VARCHAR(120) NOT NULL,
                address VARCHAR(255),
                phone_number VARCHAR(40),
                email_address VARCHAR(255),
                open_time DATETIME,
                close_time DATETIME
            )
            """;

    public static final String FIND_BY_ID = """
            SELECT id, name, address, phone_number, email_address, open_time, close_time
            FROM providers
            WHERE id = ?
            """;

    public static final String UPSERT = """
            INSERT INTO providers (id, name, address, phone_number, email_address, open_time, close_time)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                address = VALUES(address),
                phone_number = VALUES(phone_number),
                email_address = VALUES(email_address),
                open_time = VALUES(open_time),
                close_time = VALUES(close_time)
            """;
}
