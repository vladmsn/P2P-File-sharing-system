package org._ubb.view;

import org._ubb.model.Node;

import java.util.Scanner;

public class CommandLineInterface {

    private final Node node;
    private final Scanner scanner;

    public CommandLineInterface(Node node) {
        this.node = node;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("Enter command (type 'help' for options): ");
            String command = scanner.nextLine();

            switch (command) {
                case "help":
                    printHelp();
                    break;
                case "connect":
                    // Connect to network or other nodes
                    break;
                case "exit":
                    running = false;
                    break;
                default:
                    System.out.println("Unknown command");
            }
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("help - Show this help message");
        System.out.println("connect - Connect to the network");
        System.out.println("exit - Exit the application");
    }

}
