package command.exceptions;

/**
 * This Exception should be thrown when a Command is executed with invalid parameters.
 */
public class InvalidParameterException extends Exception {
    /**
     * This constructor calls the Exception constructor and sets the message.
     * @param message Exception message
     */
    public InvalidParameterException(String message) {
        super(message);
    }
}
