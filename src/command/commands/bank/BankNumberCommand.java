package command.commands.bank;

import command.Command;
import command.exceptions.InvalidParameterException;
import database.tables.BankAccount;
import util.FileLogger;

import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This Command retrieves the amount of bank accounts in the database.
 */
public class BankNumberCommand extends Command {
    /**
     * This constructor sets the Command name to BN.
     */
    public BankNumberCommand() {
        super("BN");
    }

    @Override
    public void execute(Object[] args) {
        Socket socket = (Socket)args[0];
        PrintWriter out = (PrintWriter)args[1];
        String paramString = (String)args[2];
        Connection connection = (Connection)args[3];

        // If the connection doesn't exist, database can't be accessed.
        if (connection == null) {
            out.print("ER Failed to access database.\r\n");
            out.flush();
            FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                    + " could not connect to the database.");
            return;
        }

        try {
            parseParameters(paramString);
        } catch (InvalidParameterException e) {
            out.print("ER " + e.getMessage() + "\r\n");
            out.flush();
            FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                    + " used invalid parameters with command " + this.name + ".");
            return;
        }

        try {
            int amount = BankAccount.getAccountAmount(connection);
            out.print(this.name + " " + amount + "\r\n");
            out.flush();
            FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort() + " used command " + this.name + ".");
        } catch (SQLException e) {
            out.print("ER Failed to retrieve amount of bank accounts.\r\n");
            out.flush();
            FileLogger.getLogger().severe("Peer at " + socket.getInetAddress() + ":" + socket.getPort() + " used command "
                    + this.name + ", but failed to retrieve the amount of bank accounts from the database.");
        }
    }

    /**
     * Parses the parameter string. The BankNumberCommand does not expect any parameters.
     * If parameters are present, an InvalidParameterException is thrown.
     * @param paramString String of parameters to be parsed
     * @return Array of type Object, however this method always returns null
     * @throws InvalidParameterException Invalid parameters were used
     */
    @Override
    public Object[] parseParameters(String paramString) throws InvalidParameterException {
        if (paramString != null) {
            throw new InvalidParameterException("Invalid parameters (usage: " + this.name + ").");
        }
        return null;
    }
}
