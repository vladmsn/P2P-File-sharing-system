package org._ubb;

import org._ubb.config.ConfigLoader;
import org._ubb.config.NodeConfig;
import org._ubb.model.Node;
import org._ubb.view.CommandLineInterface;

public class Main {
    public static void main(String[] args) {
        ConfigLoader configLoader = new ConfigLoader();
        NodeConfig config = configLoader.loadConfig();
        Node node = new Node("localhost", config.getLocalNodePort()); // Adjust initialization as needed

        CommandLineInterface cli = new CommandLineInterface(node);
        cli.start();
    }
}