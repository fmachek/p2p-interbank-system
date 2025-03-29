package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * This class contains some methods which load data from a configuration file.
 */
public class ConfigLoader {
    /**
     * Loads the database credentials from a configuration file.
     * @param configFilePath Path to the config file
     * @return HashMap of property names and their values - address, database, username and password
     * @throws IOException Error occurred while reading configuration file
     * @throws IllegalArgumentException Missing or blank database credentials in the configuration file
     */
    public HashMap<String, String> loadDatabaseCredentials(String configFilePath) throws IOException, IllegalArgumentException {
        FileInputStream fileInputStream = new FileInputStream(configFilePath);
        Properties properties = new Properties();
        properties.load(fileInputStream);
        fileInputStream.close();

        String address = properties.getProperty("address");
        String database = properties.getProperty("database");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        if(address == null || database == null || username == null || password == null) {
            throw new IllegalArgumentException("There are keys missing in the configuration file.");
        }

        if(address.isBlank() || database.isBlank() || username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("There are values missing in the configuration file.");
        }

        HashMap<String, String> propertyDictionary = new HashMap<>();
        propertyDictionary.put("address", address);
        propertyDictionary.put("database", database);
        propertyDictionary.put("username", username);
        propertyDictionary.put("password", password);

        return propertyDictionary;
    }

    /**
     * Loads peer to peer settings from a configuration file.
     * @param configFilePath Path to the config file
     * @return HashMap of property names and their values - hostAddress, port
     * @throws IOException Error occurred while reading configuration file
     * @throws IllegalArgumentException Missing or blank P2P credentials in the configuration file
     */
    public HashMap<String, String> loadPeerSettings(String configFilePath) throws IOException, IllegalArgumentException {
        FileInputStream fileInputStream = new FileInputStream(configFilePath);
        Properties properties = new Properties();
        properties.load(fileInputStream);
        fileInputStream.close();

        String hostAddress = properties.getProperty("host_address");
        String port = properties.getProperty("port");

        if(hostAddress == null || port == null) {
            throw new IllegalArgumentException("There are keys missing in the configuration file.");
        }

        if(hostAddress.isBlank() || port.isBlank()) {
            throw new IllegalArgumentException("There are values missing in the configuration file.");
        }

        HashMap<String, String> propertyDictionary = new HashMap<>();
        propertyDictionary.put("hostAddress", hostAddress);
        propertyDictionary.put("port", port);

        return propertyDictionary;
    }
}
