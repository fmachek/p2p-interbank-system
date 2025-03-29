package peer;

import database.DatabaseConnector;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

/**
 * This class represents a peer connected to the peer hosting the program.
 * It contains the peer socket, I/O objects used for communicating with the peer,
 * and the database connection assigned to the peer.
 */
public class ClientPeer {
    private final Socket peerSocket;
    private final Connection connection;
    private final InputStreamReader reader;
    private final BufferedReader in;
    private final OutputStreamWriter writer;
    private final PrintWriter out;

    /**
     * This private constructor sets the required properties.
     * @param peerSocket Peer socket
     * @param connection Database connection
     * @param reader InputStreamReader instance
     * @param in BufferedReader instance, used to accept messages from the peer
     * @param writer OutputStreamWriter instance
     * @param out PrintWriter instance, used to send messages to the peer
     */
    private ClientPeer(Socket peerSocket, Connection connection, InputStreamReader reader, BufferedReader in, OutputStreamWriter writer, PrintWriter out) {
        this.peerSocket = peerSocket;
        this.connection = connection;
        this.reader = reader;
        this.in = in;
        this.writer = writer;
        this.out = out;
    }

    /**
     * Creates a new ClientPeer instance with the given peer socket.
     * It also creates the necessary objects such as the database connection (may be null),
     * and the I/O objects.
     * @param peerSocket Connected peer socket
     * @return New ClientPeer instance, or null if failed
     */
    public static ClientPeer create(Socket peerSocket) {
        try {
            Connection connection = DatabaseConnector.getInstance().getConnection();
            InputStreamReader reader = new InputStreamReader(peerSocket.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(reader);
            OutputStreamWriter writer = new OutputStreamWriter(peerSocket.getOutputStream(), StandardCharsets.UTF_8);
            PrintWriter out = new PrintWriter(writer, true);
            return new ClientPeer(peerSocket, connection, reader, in, writer, out);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Closes the I/O objects used to communicate with the peer.
     * @throws IOException
     */
    public void closeIO() throws IOException {
        out.close();
        in.close();
        writer.close();
        reader.close();
    }

    /**
     * Returns the BufferedReader instance.
     * @return BufferedReader instance
     */
    public BufferedReader getIn() {
        return in;
    }

    /**
     * Returns the PrintWriter instance.
     * @return PrintWriter instance
     */
    public PrintWriter getOut() {
        return out;
    }

    /**
     * Returns the peer socket.
     * @return Peer socket
     */
    public Socket getPeerSocket() {
        return peerSocket;
    }

    /**
     * Returns the peer's database connection.
     * @return Database connection
     */
    public Connection getConnection() {
        return connection;
    }
}
