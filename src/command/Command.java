package command;

import command.exceptions.InvalidParameterException;

/**
 * This class represents a Command in the Command design pattern.
 */
public abstract class Command {
    protected final String name;

    /**
     * Constructor which sets the Command name, which is used to call it.
     * @param name Command name
     */
    public Command(String name) {
        this.name = name;
    }

    /**
     * This function performs the Command action.
     * @param args Array of type Object. Different Command classes require different arguments.
     *             For instance, some may require a client socket or database connection.
     */
    public abstract void execute(Object[] args);

    /**
     * Every Command class must implement this method. It takes the parameter string
     * and parses it. Every Command class may have different parameter requirements.
     * @param paramString String of parameters to be parsed
     * @return Array of parsed parameters
     */
    public abstract Object[] parseParameters(String paramString) throws InvalidParameterException;

    /**
     * Returns the Command name, which is used to call it.
     * @return Command name
     */
    public String getName() {
        return name;
    }
}
