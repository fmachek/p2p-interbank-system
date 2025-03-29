package command.commands.bank;

import command.Command;
import command.exceptions.InvalidParameterException;
import util.FileLogger;

import java.io.PrintWriter;
import java.net.Socket;

/**
 * This Command sends the bank code to the peer. The bank code is the IP address the
 * host socket is running on.
 */
public class BankCodeCommand extends Command {
    private final String bankCode;

    /**
     * This constructor sets the Command name to BC.
     * @param bankCode Bank code, which is the IP address the host socket is running on
     */
    public BankCodeCommand(String bankCode) {
        super("BC");
        this.bankCode = bankCode;
    }

    /**
     * Sends the bank code to the peer.
     * @param args Array of type Object. This Command expects a peer Socket, PrintWriter, and parameter String.
     */
    @Override
    public void execute(Object[] args) {
        Socket socket = (Socket)args[0];
        PrintWriter out = (PrintWriter)args[1];
        String paramString = (String)args[2];
        try {
            parseParameters(paramString);
        } catch (InvalidParameterException e) {
            out.print("ER " + e.getMessage() + "\r\n");
            out.flush();
            FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort()
                    + " used invalid parameters with command " + this.name + ".");
            return;
        }
        out.print(this.name + " " + bankCode + "\r\n");
        out.flush();
        FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort() + " used command " + this.name + ".");
    }

    /**
     * Parses the parameter string. The BankCodeCommand does not expect any parameters.
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
