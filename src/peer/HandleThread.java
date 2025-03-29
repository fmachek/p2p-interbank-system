package peer;

import command.Command;
import command.util.CommandManager;
import util.FileLogger;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Connection;

/**
 * This custom Thread class handles a single peer and handles the messages
 * sent by them. It executes the Commands called by the peer.
 */
public class HandleThread extends Thread {
    private final HostPeer host;
    private final ClientPeer peer;
    private final Socket peerSocket;
    private final Connection connection;

    /**
     * Constructor which sets the host, peer being handled, peer socket and peer's database connection.
     * @param host Host peer
     * @param peer Peer connected to the host
     */
    public HandleThread(HostPeer host, ClientPeer peer) {
        this.host = host;
        this.peerSocket = peer.getPeerSocket();
        this.peer = peer;
        this.connection = peer.getConnection();
    }

    /**
     * Calls the handlePeer() method.
     */
    @Override
    public void run() {
        try {
            handlePeer();
        } catch (IOException e) {
            FileLogger.getLogger().info("IOException occurred while communicating with peer at " +
                    peerSocket.getInetAddress() + ":" + peerSocket.getPort() +".");
        }
    }

    /**
     * Handles the given peer connected to the host peer. Accepts messages from the peer and passes them to the
     * handleMessage() method. If the message is null, the peer is disconnected. The thread also catches
     * the SocketTimeoutException and disconnects the peer.
     * @throws IOException An I/O operation failed while communicating with the peer
     */
    private void handlePeer() throws IOException {
        BufferedReader in = peer.getIn();
        PrintWriter out = peer.getOut();
        Socket peerSocket = peer.getPeerSocket();

        while (true) {
            try {
                String message = in.readLine();
                if (message == null) { // Received null, disconnect the peer
                    host.disconnectPeer(peerSocket);
                    peer.closeIO();
                    FileLogger.getLogger().info("Received empty message from peer at " +
                            peerSocket.getInetAddress() + ":" + peerSocket.getPort() +", disconnecting.");
                    break;
                }
                handleMessage(message, out);
            } catch (SocketTimeoutException e) {
                FileLogger.getLogger().info("Peer at " + peerSocket.getInetAddress() + ":" + peerSocket.getPort() +", has been timed out," +
                        " disconnecting.");
                host.disconnectPeer(peerSocket);
                peer.closeIO();
                break;
            }
        }
    }

    /**
     * Handles a message received from a peer.
     * @param message Message received from a peer
     * @param out PrintWriter used to communicate with the peer
     */
    private void handleMessage(String message, PrintWriter out) {
        String[] substrings = message.split(" ", 2);
        String commandName = null;
        String paramString = null;

        if (substrings.length == 1) {
            commandName = substrings[0].strip().toUpperCase();
        } else if (substrings.length > 1) {
            commandName = substrings[0].strip().toUpperCase();
            paramString = substrings[1].strip();
        }

        Command command = CommandManager.getInstance().getCommand(commandName);
        if (command == null) {
            out.print("ER Command not found.\r\n");
            out.flush();
            return;
        }

        // Create the args array of Objects and pass it to the command
        // Some of the arguments may not be used by the command
        Object[] args = new Object[4];
        args[0] = peerSocket;
        args[1] = out;
        args[2] = paramString;
        args[3] = connection;

        command.execute(args);
    }
}
