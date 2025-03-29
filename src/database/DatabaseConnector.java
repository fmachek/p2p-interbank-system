package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class implements the Singleton design pattern and can be configured to
 * easily create new database connections.
 */
public class DatabaseConnector {
    private static DatabaseConnector instance;
    private Boolean configured = false;
    private String url;
    private String user;
    private String password;

    /**
     * Returns the Singleton instance of DatabaseConnector.
     * @return Singleton instance of DatabaseConnector
     */
    public static DatabaseConnector getInstance() {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    /**
     * Configures the DatabaseConnector so that it can create new database connections.
     * @param dbAddress Database address
     * @param dbName Database name
     * @param dbUser Database user
     * @param dbPassword Database password
     */
    public void configure(String dbAddress, String dbName, String dbUser, String dbPassword) {
        if (!configured) {
            this.url = "jdbc:sqlserver://" + dbAddress + ";databaseName=" + dbName + ";encrypt=true;trustServerCertificate=true;";
            this.user = dbUser;
            this.password = dbPassword;
            configured = true;
        }
    }

    /**
     * Creates a new Connection if it has been configured. It uses that configuration
     * to create the new Connection.
     * @return New database connection, or null if an error occurs or the DatabaseConnector has not been configured
     */
    public Connection getConnection() {
        if (configured) {
            try {
                return DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
