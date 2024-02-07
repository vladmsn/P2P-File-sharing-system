package org._ubb.view;

import org._ubb.model.ClientNode;
import org._ubb.model.TorrentFileDto;
import org._ubb.service.FileTransferService;
import org._ubb.utils.EncryptionUtils;

import java.util.List;
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
        this.fileTransferService.connectToNetwork();
    }

    private void uploadFile() {
        System.out.println("Enter file path of the file you want to upload:");
        String filepath = scanner.nextLine();
        try {
            String identifier = EncryptionUtils.generateFileId(filepath);
            this.clientNode.addToDataStore(identifier, filepath);
            this.fileTransferService.uploadFile(identifier, filepath);
            System.out.println("File uploaded successfully. File identifier: " + identifier);
        } catch (Exception e) {
            System.out.println("invalid file path");
        }
    }

    private void downloadFile() {
        System.out.println("Enter file identifier of the file you want to download:");
        String fileId = scanner.nextLine();
        try {
            this.fileTransferService.downloadFile(fileId);
        } catch (Exception e) {
            System.out.println("Failed to download file: " + e.getMessage());
        }
    }

    private void listFiles() {

        try {
            List<TorrentFileDto> files = this.fileTransferService.getFiles();

            for (var file : files) {
                System.out.println(file.toString());
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch files: " + e.getMessage());
        }
    }

}
