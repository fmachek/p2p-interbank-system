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
 * This Command retrieves the total bank balance (sum of all bank account balance).
 */
public class BankAmountCommand extends Command {
    /**
     * Constructor which sets the Command name to BA.
     */
    public BankAmountCommand() {
        super("BA");
    }

    /**
     * Attempts to retrieve the total balance from the database, and send it to the peer.
     * If it fails, an error message is sent to the peer. This Command does not expect any parameters.
     * Using parameters will result in an InvalidParameterException being thrown and an error message
     * being sent to the peer.
     *
     * @param args Array of type Object. This Command expects a peer Socket, PrintWriter, parameter String, and Connection.
     */
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
            int total = BankAccount.getTotalBalance(connection);
            out.print(this.name + " " + total + "\r\n");
            out.flush();
            FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort() + " used command " + this.name + ".");
        } catch (SQLException e) {
            out.print("ER Failed to retrieve bank amount.\r\n");
            out.flush();
            FileLogger.getLogger().severe("Peer at " + socket.getInetAddress() + ":" + socket.getPort() + " used command "
                    + this.name + ", but failed to retrieve the bank amount from the database.");
        }
    }

    /**
     * Parses the parameter string. The BankAmountCommand does not expect any parameters.
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
