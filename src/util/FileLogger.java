package util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This class encapsulates a Logger instance and sets it up.
 */
public class FileLogger {
    private static final Logger logger = Logger.getLogger("MainLogger");

    static {
        try {
            FileHandler fileHandler = new FileHandler("node.log", true);
            fileHandler.setFormatter(new SimpleFormatter());

            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "File logging failed.", e);
        }
    }

    /**
     * Returns the Logger instance.
     * @return The Logger instance
     */
    public static Logger getLogger() {
        return logger;
    }
}
