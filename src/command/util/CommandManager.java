package command.util;

import command.Command;
import java.util.HashMap;

/**
 * This class implements the Singleton design pattern. It contains a HashMap where the keys are
 * Command names, and the values are Commands.
 */
public class CommandManager {
    private static CommandManager instance;
    private final HashMap<String, Command> commands = new HashMap<>();

    /**
     * Returns the Singleton instance of CommandManager.
     * @return Singleton instance of CommandManager
     */
    public static CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }

    /**
     * Registers a new Command if it is not present in the HashMap yet.
     * @param command Command being registered
     */
    public void registerCommand(Command command) {
        String commandName = command.getName();
        if (!commands.containsKey(commandName)) {
            commands.put(commandName, command);
        }
    }

    /**
     * Returns the Command with the given name, if it exists.
     * @param commandName Command name
     * @return Command if exists, otherwise null
     */
    public Command getCommand(String commandName) {
        if (!commands.containsKey(commandName)) {
            return null;
        }
        return commands.get(commandName);
    }
}
