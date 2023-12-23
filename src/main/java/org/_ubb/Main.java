package org._ubb;

import org._ubb.config.ConfigLoader;
import org._ubb.config.NodeConfig;
import org._ubb.model.ClientNode;
import org._ubb.network.NetworkHandler;
import org._ubb.service.FileTransferService;
import org._ubb.view.CommandLineInterface;

public class Main {
    public static void main(String[] args) {
        ConfigLoader configLoader = new ConfigLoader();
        NodeConfig config = configLoader.loadConfig();

        ClientNode clientNode = new ClientNode("127.0.0.1", config.getLocalNodePort());
        NetworkHandler networkHandler = new NetworkHandler(config.getLocalNodePort());
        FileTransferService fileTransferService = new FileTransferService(clientNode, networkHandler);

        CommandLineInterface cli = new CommandLineInterface(clientNode, fileTransferService);
        cli.start();
    }
}