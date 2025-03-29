package peer;

import util.FileLogger;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class represents the Peer who is hosting the program.
 * It accepts other peers' sockets and passes them to new threads.
 */
public class HostPeer {
    private final InetAddress address;
    private final int port;
    private final int backlog;
    private final ArrayList<Socket> peers = new ArrayList<>();

    /**
     * This constructor sets the IP address, port and backlog.
     * @param address IP address the program will run on
     * @param port Port the program will run on
     * @param backlog Max incoming connections
     */
    public HostPeer(InetAddress address, int port, int backlog) {
        this.address = address;
        this.port = port;
        this.backlog = backlog;
    }

    /**
     * Creates a new ServerSocket with the configured port, backlog and address.
     * New peer sockets are accepted in a loop, and they are passed to a new HandleThread,
     * which will handle the communication with the peer.
     */
    public void start() {
        try (ServerSocket socket = new ServerSocket(port, backlog, address)) {
            FileLogger.getLogger().info("Server started on " + socket.getInetAddress() + ":" + socket.getLocalPort() + ".");
            while (true) {
                Socket peerSocket = socket.accept();
                peerSocket.setSoTimeout(60000);
                peers.add(peerSocket);
                FileLogger.getLogger().info("Peer at " + peerSocket.getInetAddress() + ":" + peerSocket.getPort() + " connected.");

                ClientPeer peer = ClientPeer.create(peerSocket);

                if (peer == null) {
                    FileLogger.getLogger().severe("An error occurred while communicating with " +
                            "peer at " + peerSocket.getInetAddress() + ":" + peerSocket.getPort() + ".");
                    disconnectPeer(peerSocket);
                    continue;
                }

                HandleThread handleThread = new HandleThread(this, peer);
                handleThread.start();
            }
        } catch (IOException e) {
            FileLogger.getLogger().severe("IOException occurred while starting ServerSocket.");
        } catch (IllegalArgumentException ex) {
            FileLogger.getLogger().severe("Failed to start ServerSocket due to invalid port.");
        }
    }

    /**
     * Disconnects a connected peer.
     * @param socket Peer socket
     */
    public void disconnectPeer(Socket socket) {
        try {
            socket.close();
            peers.remove(socket);
            FileLogger.getLogger().info("Peer at " + socket.getInetAddress() + ":" + socket.getPort() + " disconnected.");
        } catch (IOException e) {
            FileLogger.getLogger().severe("IOException occurred while disconnecting peer" +
                    " at " + socket.getInetAddress() + ":" + socket.getPort() + ".");
        }
    }
}
