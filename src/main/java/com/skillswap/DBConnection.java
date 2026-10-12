package com.skillswap;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages JDBC database connectivity to the MySQL database.
 * Reads configuration from:
 * 1. Programmatic override (configure)
 * 2. System properties (db.url, db.user, db.password)
 * 3. Environment variables (DB_URL, DB_USER, DB_PASSWORD)
 * 4. application.properties (classpath)
 * 
 * Never logs or prints passwords to ensure security.
 */
public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/skillswap";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PARAMS = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";

    private static final Properties props = new Properties();

    // Programmatic override support (useful for configuration or testing)
    private static String customUrl = null;
    private static String customUser = null;
    private static String customPassword = null;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL ERROR: MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found on classpath!");
            System.err.println("Ensure mysql-connector-j is included in pom.xml and Maven dependencies are resolved.");
        }

        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Programmatically configure database credentials if needed.
     */
    public static synchronized void configure(String url, String user, String password) {
        customUrl = url;
        customUser = user;
        customPassword = password;
    }

    /**
     * Resolves the database JDBC URL.
     */
    public static String getDbUrl() {
        if (customUrl != null && !customUrl.trim().isEmpty()) {
            return formatUrl(customUrl);
        }
        String sysUrl = System.getProperty("db.url");
        if (sysUrl != null && !sysUrl.trim().isEmpty()) {
            return formatUrl(sysUrl);
        }
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return formatUrl(envUrl);
        }
        String propUrl = props.getProperty("db.url");
        if (propUrl == null) {
            propUrl = props.getProperty("spring.datasource.url");
        }
        if (propUrl != null && !propUrl.trim().isEmpty()) {
            return formatUrl(propUrl);
        }
        return formatUrl(DEFAULT_URL);
    }

    /**
     * Resolves the database username.
     */
    public static String getDbUser() {
        if (customUser != null && !customUser.trim().isEmpty()) {
            return customUser;
        }
        String sysUser = System.getProperty("db.user");
        if (sysUser != null && !sysUser.trim().isEmpty()) {
            return sysUser;
        }
        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.trim().isEmpty()) {
            return envUser;
        }
        String propUser = props.getProperty("db.user");
        if (propUser == null) {
            propUser = props.getProperty("spring.datasource.username");
        }
        if (propUser != null && !propUser.trim().isEmpty()) {
            return propUser.trim();
        }
        return DEFAULT_USER;
    }

    /**
     * Resolves the database password securely without exposing it.
     */
    private static String getDbPassword() {
        if (customPassword != null) {
            return customPassword;
        }
        String sysPass = System.getProperty("db.password");
        if (sysPass != null) {
            return sysPass;
        }
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) {
            return envPass;
        }
        String propPass = props.getProperty("db.password");
        if (propPass == null) {
            propPass = props.getProperty("spring.datasource.password");
        }
        if (propPass != null) {
            return propPass.trim();
        }
        return "";
    }

    /**
     * Appends required connection query parameters if absent.
     */
    private static String formatUrl(String rawUrl) {
        if (rawUrl == null) return DEFAULT_URL + DEFAULT_PARAMS;
        if (!rawUrl.contains("?")) {
            return rawUrl + DEFAULT_PARAMS;
        }
        return rawUrl;
    }

    /**
     * Establishes and returns a new JDBC Connection.
     * Callers must close the Connection using try-with-resources.
     *
     * @return an open {@link Connection} to MySQL
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        String url = getDbUrl();
        String user = getDbUser();
        String password = getDbPassword();

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            String sanitizedMessage = String.format(
                "Failed to connect to MySQL database at '%s' as user '%s'. (SQLState: %s, ErrorCode: %d). Cause: %s",
                url, user, e.getSQLState(), e.getErrorCode(), e.getMessage()
            );
            throw new SQLException(sanitizedMessage, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    /**
     * Tests connectivity to MySQL and returns true if successful.
     * Never prints or logs the database password.
     */
    public static boolean testConnection() {
        String url = getDbUrl();
        String user = getDbUser();
        System.out.println("[DBConnection] Attempting connection to: " + url + " [User: " + user + "]");

        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("[DBConnection] SUCCESS: Connected to MySQL database successfully! (" 
                        + conn.getMetaData().getDatabaseProductName() + " " 
                        + conn.getMetaData().getDatabaseProductVersion() + ")");
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("[DBConnection] FAILED: " + e.getMessage());
            System.err.println("[DBConnection] TIP: Ensure MySQL Server is running and credentials are set via DB_PASSWORD environment variable.");
            return false;
        }
    }
}
