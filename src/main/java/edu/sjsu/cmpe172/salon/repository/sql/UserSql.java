package edu.sjsu.cmpe172.salon.repository.sql;

public final class UserSql {
    private UserSql() {
    }

    public static final String CREATE_USERS_TABLE = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                email_address VARCHAR(255) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL
            )
            """;

    public static final String CREATE_SPECIALITIES_TABLE = """
            CREATE TABLE IF NOT EXISTS specialities (
                id INT PRIMARY KEY,
                code VARCHAR(50) NOT NULL UNIQUE,
                display_name VARCHAR(120) NOT NULL
            )
            """;

    public static final String CREATE_CUSTOMERS_TABLE = """
            CREATE TABLE IF NOT EXISTS customers (
                user_id INT PRIMARY KEY,
                phone_number VARCHAR(40),
                CONSTRAINT fk_customers_users
                    FOREIGN KEY (user_id) REFERENCES users(id)
                    ON DELETE CASCADE
            )
            """;

    public static final String CREATE_STYLISTS_TABLE = """
            CREATE TABLE IF NOT EXISTS stylists (
                user_id INT PRIMARY KEY,
                speciality_id INT NOT NULL,
                CONSTRAINT fk_stylists_users
                    FOREIGN KEY (user_id) REFERENCES users(id)
                    ON DELETE CASCADE,
                CONSTRAINT fk_stylists_specialities
                    FOREIGN KEY (speciality_id) REFERENCES specialities(id)
            )
            """;

    public static final String CREATE_ADMINS_TABLE = """
            CREATE TABLE IF NOT EXISTS admins (
                user_id INT PRIMARY KEY,
                CONSTRAINT fk_admins_users
                    FOREIGN KEY (user_id) REFERENCES users(id)
                    ON DELETE CASCADE
            )
            """;

    public static final String UPSERT_SPECIALITY = """
            INSERT INTO specialities (id, code, display_name)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
                code = VALUES(code),
                display_name = VALUES(display_name)
            """;

    public static final String FIND_BY_EMAIL = """
            SELECT
                u.id,
                u.first_name,
                u.last_name,
                u.email_address,
                u.password_hash,
                c.phone_number,
                s.speciality_id,
                CASE
                    WHEN a.user_id IS NOT NULL THEN 100
                    WHEN s.user_id IS NOT NULL THEN 2
                    WHEN c.user_id IS NOT NULL THEN 1
                    ELSE 1
                END AS role_value
            FROM users u
            LEFT JOIN customers c ON c.user_id = u.id
            LEFT JOIN stylists s ON s.user_id = u.id
            LEFT JOIN admins a ON a.user_id = u.id
            WHERE u.email_address = ?
            """;

    public static final String FIND_BY_ID = """
            SELECT
                u.id,
                u.first_name,
                u.last_name,
                u.email_address,
                u.password_hash,
                c.phone_number,
                s.speciality_id,
                CASE
                    WHEN a.user_id IS NOT NULL THEN 100
                    WHEN s.user_id IS NOT NULL THEN 2
                    WHEN c.user_id IS NOT NULL THEN 1
                    ELSE 1
                END AS role_value
            FROM users u
            LEFT JOIN customers c ON c.user_id = u.id
            LEFT JOIN stylists s ON s.user_id = u.id
            LEFT JOIN admins a ON a.user_id = u.id
            WHERE u.id = ?
            """;

    public static final String FIND_ALL = """
            SELECT
                u.id,
                u.first_name,
                u.last_name,
                u.email_address,
                u.password_hash,
                c.phone_number,
                s.speciality_id,
                CASE
                    WHEN a.user_id IS NOT NULL THEN 100
                    WHEN s.user_id IS NOT NULL THEN 2
                    WHEN c.user_id IS NOT NULL THEN 1
                    ELSE 1
                END AS role_value
            FROM users u
            LEFT JOIN customers c ON c.user_id = u.id
            LEFT JOIN stylists s ON s.user_id = u.id
            LEFT JOIN admins a ON a.user_id = u.id
            ORDER BY u.id
            """;

    public static final String FIND_ALL_STYLISTS = """
            SELECT
                u.id,
                u.first_name,
                u.last_name,
                u.email_address,
                u.password_hash,
                NULL AS phone_number,
                s.speciality_id,
                2 AS role_value
            FROM users u
            INNER JOIN stylists s ON s.user_id = u.id
            ORDER BY u.first_name, u.last_name
            """;

    public static final String INSERT_USER = """
            INSERT INTO users (first_name, last_name, email_address, password_hash)
            VALUES (?, ?, ?, ?)
            """;

    public static final String INSERT_CUSTOMER = """
            INSERT INTO customers (user_id, phone_number)
            VALUES (?, ?)
            """;

    public static final String FIND_USER_ID_BY_EMAIL = """
            SELECT id FROM users WHERE email_address = ?
            """;

    public static final String INSERT_ADMIN = """
            INSERT INTO admins (user_id)
            VALUES (?)
            ON DUPLICATE KEY UPDATE user_id = VALUES(user_id)
            """;

    public static final String USER_EXISTS = """
            SELECT 1 FROM users WHERE id = ?
            """;

    public static final String IS_ADMIN = """
            SELECT 1 FROM admins WHERE user_id = ?
            """;

    public static final String DELETE_CUSTOMER_ROLE = """
            DELETE FROM customers WHERE user_id = ?
            """;

    public static final String DELETE_STYLIST_ROLE = """
            DELETE FROM stylists WHERE user_id = ?
            """;

    public static final String UPSERT_STYLIST = """
            INSERT INTO stylists (user_id, speciality_id)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE
                speciality_id = VALUES(speciality_id)
            """;
}
