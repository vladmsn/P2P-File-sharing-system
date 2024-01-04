package org._ubb.view;

import org._ubb.model.ClientNode;
import org._ubb.service.FileTransferService;
import org._ubb.utils.EncryptionUtils;

import java.util.Scanner;

public class CommandLineInterface {

    private final ClientNode clientNode;
    private final FileTransferService fileTransferService;
    private final Scanner scanner;

    public CommandLineInterface(ClientNode clientNode, FileTransferService fileTransferService) {
        this.clientNode = clientNode;
        this.fileTransferService = fileTransferService;
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
                    connectToNetwork();
                    break;
                case "upload":
                    uploadFile();
                    break;
                case "download":
                    downloadFile();
                    break;
                case "list":
                    listFiles();
                    break;
                case "ping":
                    System.out.println("Enter node address:");
                    String address = scanner.nextLine();
                    fileTransferService.pingNode(address);
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
        System.out.println("upload - Upload a file to the network");
        System.out.println("download - Download a file from the network");
        System.out.println("list - List available files");
        System.out.println("exit - Exit the application");
    }

    private void connectToNetwork() {
        // Implementation to connect to the network
    }

    private void uploadFile() {
        System.out.println("Enter file path of the file you want to upload:");
        String filepath = scanner.nextLine();
        try {
            String identifier = EncryptionUtils.generateFileId(filepath);
            this.clientNode.addToDataStore(identifier, filepath);
            System.out.println("File uploaded successfully. File identifier: " + identifier);
        } catch (Exception e) {
            System.out.println("invalid file path");
        }
    }

    private void downloadFile() {
        System.out.println("Enter file identifier of the file you want to download:");
        String fileId = scanner.nextLine();
        this.fileTransferService.downloadFile(fileId, "127.0.0.1:8080");
    }

    private void listFiles() {
        // For now the files that the current client has
        System.out.println("Files available for download:");
        for (String fileId : this.clientNode.getDataStore().keySet()) {
            System.out.println(fileId);
        }
    }

}
