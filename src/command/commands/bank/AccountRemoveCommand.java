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
 * This Command removes a bank account with a given account number.
 */
public class AccountRemoveCommand extends Command implements GeneralCommandParser {
    private final String bankCode;

    /**
     * This constructor sets the Command name to AR, and the bank code.
     * @param bankCode Bank code (IP address)
     */
    public AccountRemoveCommand(String bankCode) {
        super("AR");
        this.bankCode = bankCode;
    }

    /**
     * Executes the Command action. The parameters are parsed and the bank account with the
     * given account number is retrieved. The account is then deleted. If an error occurs,
     * an error message is sent to the peer instead. If the peer's database connection has not been established,
     * an error message is sent to the peer.
     * @param args Array of type Object. This Command expects a Socket, PrintWriter, parameter String, and database Connection.
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

        Object[] parameters;
        try {
            parameters = parseParameters(paramString);
        } catch (InvalidParameterException e) {
            out.print("ER " + e.getMessage() + "\r\n");
            out.flush();
            FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                    + " used invalid parameters with command " + this.name + ".");
            return;
        }

        int accountNumber = (int)parameters[0];
        String bankCode = (String)parameters[1];

        if (!bankCode.equals(this.bankCode)) {
            out.print("ER Incorrect bank code.\r\n");
            out.flush();
            FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                    + " used incorrect bank code for command " + this.name + ".");
            return;
        }

        try {
            // Get account with given account number
            BankAccount account = BankAccount.findByAccountNumber(accountNumber, connection);
            if (account == null) {
                out.print("ER Account not found.\r\n");
                out.flush();
                FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                        + " used incorrect account number for command " + this.name + ".");
                return;
            }
            try {
                account.delete(connection);
                out.print(this.name + "\r\n");
                out.flush();
                FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                        + " deleted account with number " + accountNumber + ".");
            } catch (SQLException e) {
                out.print("ER Database error occurred, failed to delete account.\r\n");
                out.flush();
                FileLogger.getLogger().severe("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                        + " failed to delete account from the database while using command " + this.name + ".");
            }
        } catch (SQLException e) {
            out.print("ER Database error occurred, failed to delete account.\r\n");
            out.flush();
            FileLogger.getLogger().severe("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                    + " failed to retrieve account from the database while using command " + this.name + ".");
        }
    }

    /**
     * Parses the parameters provided to the Command. This Command
     * expects parameters in this format: [account_number]/[bank_code]
     * @param paramString String of parameters to be parsed
     * @return Array of Objects (account number (int) and bank code (String))
     * @throws InvalidParameterException Invalid or no parameters
     */
    @Override
    public Object[] parseParameters(String paramString) throws InvalidParameterException {
        return parseAccountNumberAndBankCode(paramString, this.name);
    }
}
