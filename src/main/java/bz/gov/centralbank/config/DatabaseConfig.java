package bz.gov.centralbank.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centralized PostgreSQL connection factory.
 * Uses environment variables for credentials and connection details to avoid
 * hard-coding secrets in source code.
 */
public final class DatabaseConfig {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_DB = "central_bank";
    private static final String DEFAULT_USER = "central_bank_app";
    private static final String DEFAULT_PASSWORD = "change_me";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC driver not found", e);
        }
    }

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        String host = getenvOrDefault("CBZ_DB_HOST", DEFAULT_HOST);
        String port = getenvOrDefault("CBZ_DB_PORT", DEFAULT_PORT);
        String db = getenvOrDefault("CBZ_DB_NAME", DEFAULT_DB);
        String user = getenvOrDefault("CBZ_DB_USER", DEFAULT_USER);
        String password = getenvOrDefault("CBZ_DB_PASSWORD", DEFAULT_PASSWORD);

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        // Allow enabling SSL via environment variable
        props.setProperty("ssl", getenvOrDefault("CBZ_DB_SSL", "false"));

        return DriverManager.getConnection(url, props);
    }

    private static String getenvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
