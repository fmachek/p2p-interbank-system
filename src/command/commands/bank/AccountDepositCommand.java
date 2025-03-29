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
 * This Command deposits a given amount of money to a bank account.
 */
public class AccountDepositCommand extends Command implements GeneralCommandParser {
    private final String bankCode;

    /**
     * This constructor sets the Command name to AD and the bank code.
     * @param bankCode Bank code (IP address of the node)
     */
    public AccountDepositCommand(String bankCode) {
        super("AD");
        this.bankCode = bankCode;
    }

    /**
     * Executes the Command action. If the peer's database connection has not been
     * established, an error message is sent. Parameters are parsed and this Command
     * expects an account number, bank code and amount of money to be deposited.
     * The bank account is retrieved from the database and the money is deposited to it, and
     * it is updated. If an error occurs, a message is sent to the peer and it is logged.
     * @param args Array of type Object. This Command expects a Socket, PrintWriter, parameter String and database
     *             Connection (can be null).
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
        long amount = (long)parameters[2];

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
                account.deposit(amount);
                account.save(connection);
                out.print(this.name + "\r\n");
                out.flush();
                FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                        + " deposited " + amount + " balance to account with number " + accountNumber + ".");
            } catch (IllegalArgumentException e) {
                out.print("ER " + e.getMessage() + "\r\n");
                out.flush();
                FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                        + " used incorrect deposit amount for command " + this.name + ".");
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                out.print("ER Failed to deposit to the bank account.\r\n");
                out.flush();
                FileLogger.getLogger().info("A database error occurred while peer at " + socket.getInetAddress() + ":" + socket.getPort()
                        + " attempted to deposit to account using command " + this.name + ".");
            }
        } catch (SQLException e) {
            out.print("ER Database error occurred, failed to deposit.\r\n");
            out.flush();
            FileLogger.getLogger().severe("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                    + " failed to retrieve account from the database while using command " + this.name + ".");
        }
    }

    /**
     * Parses the parameters provided to the Command. This Command
     * expects parameters in this format: [account_number]/[bank_code] [amount]
     * @param paramString String of parameters to be parsed
     * @return Array of Objects (account number (int), bank code (String), amount (long))
     * @throws InvalidParameterException Invalid or no parameters
     */
    @Override
    public Object[] parseParameters(String paramString) throws InvalidParameterException {
        return parseAmount(paramString, this.name);
    }
}
