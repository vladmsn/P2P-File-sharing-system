package org._ubb;

import org._ubb.config.ConfigLoader;
import org._ubb.config.NodeConfig;
import org._ubb.config.TorrentClientConfig;
import org._ubb.model.ClientNode;
import org._ubb.network.NetworkHandler;
import org._ubb.service.FileTransferService;
import org._ubb.service.client.TorrentClient;
import org._ubb.view.CommandLineInterface;

public class Main {
    public static void main(String[] args) {
        NodeConfig config = new NodeConfig();
        TorrentClientConfig torrentClientConfig = new TorrentClientConfig();

        ClientNode clientNode = new ClientNode("127.0.0.1", config.getLocalNodePort());
        NetworkHandler networkHandler = new NetworkHandler(config.getLocalNodePort());
        TorrentClient torrentClient = new TorrentClient(torrentClientConfig);

        FileTransferService fileTransferService = new FileTransferService(clientNode, networkHandler, torrentClient);

        CommandLineInterface cli = new CommandLineInterface(clientNode, fileTransferService);
        cli.start();
    }
}