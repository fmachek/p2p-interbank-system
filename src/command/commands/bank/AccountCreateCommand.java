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
 * This Command attempts to create new bank account in the database.
 */
public class AccountCreateCommand extends Command {
    private final String bankCode;

    /**
     * This constructor sets the Command name to AC and the bank code.
     * @param bankCode Bank code (IP address)
     */
    public AccountCreateCommand(String bankCode) {
        super("AC");
        this.bankCode = bankCode;
    }

    /**
     * Attempts to create a new bank account in the database and logs errors and info using
     * the FileLogger class.
     * @param args Array of type Object. This Command expects a peer Socket, PrintWriter, database Connection, and parameter String.
     */
    @Override
    public void execute(Object[] args) {
        // Get the arguments from the args array
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
            // Get the current max account number in the database
            int max_number = BankAccount.getMaxNumber(connection);
            int account_number;
            if (max_number == 0) {
                account_number = 10000;
            } else {
                account_number = max_number + 1; // New account number will be the max + 1
            }
            if (account_number > 99999) { // Max account number reached
                out.print("ER Cannot create a new account right now.\r\n");
                out.flush();
                FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                        + " could not create a bank account.");
                return;
            }
            BankAccount account = BankAccount.create(0, account_number, 0);
            account.save(connection);
            out.print(this.name + " " + account.getAccountNumber() + "/" + bankCode + "\r\n");
            out.flush();
            FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                    + " created a new bank account with number " + account.getAccountNumber() + ".");
        } catch (SQLException e) {
            FileLogger.getLogger().severe("Failed to create bank account.");
        }
    }

    /**
     * Parses the parameter string. The AccountCreateCommand does not expect any parameters.
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
