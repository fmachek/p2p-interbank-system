import command.commands.bank.*;
import command.util.CommandManager;
import database.DatabaseConnector;
import peer.HostPeer;
import util.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        ConfigLoader configLoader = new ConfigLoader();
        String configFilePath = "config.properties";
        try {
            HashMap<String, String> dbCredentials = configLoader.loadDatabaseCredentials(configFilePath);
            HashMap<String, String> peerSettings = configLoader.loadPeerSettings(configFilePath);

            DatabaseConnector dbConnector = DatabaseConnector.getInstance();
            dbConnector.configure(
                    dbCredentials.get("address"),
                    dbCredentials.get("database"),
                    dbCredentials.get("username"),
                    dbCredentials.get("password")
            );

            String hostAddressString = peerSettings.get("hostAddress");
            int port = Integer.parseInt(peerSettings.get("port"));
            InetAddress hostAddress = InetAddress.getByName(hostAddressString);

            CommandManager commandManager = CommandManager.getInstance();
            commandManager.registerCommand(new BankAmountCommand());
            commandManager.registerCommand(new BankNumberCommand());
            commandManager.registerCommand(new BankCodeCommand(hostAddressString));
            commandManager.registerCommand(new AccountCreateCommand(hostAddressString));
            commandManager.registerCommand(new AccountDepositCommand(hostAddressString));
            commandManager.registerCommand(new AccountBalanceCommand(hostAddressString));
            commandManager.registerCommand(new AccountRemoveCommand(hostAddressString));
            commandManager.registerCommand(new AccountWithdrawalCommand(hostAddressString));

            HostPeer host = new HostPeer(hostAddress, port, 50);
            host.start();

        } catch (IOException | IllegalArgumentException e) {
            FileLogger.getLogger().severe("An error occurred when attempting to run server.");
        }
    }
}