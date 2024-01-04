package org._ubb.service;

import lombok.extern.slf4j.Slf4j;
import org._ubb.model.ClientNode;
import org._ubb.network.NetworkHandler;
import org._ubb.network.messages.AckMessage;
import org._ubb.network.messages.FileDataMessage;
import org._ubb.network.messages.FileRequestMessage;
import org._ubb.network.messages.PingMessage;
import org._ubb.network.messages.TorrentMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


@Slf4j
public class FileTransferService {

    private final ClientNode localNode;
    private final NetworkHandler networkHandler;

    public FileTransferService(ClientNode localNode, NetworkHandler networkHandler) {
        this.localNode = localNode;
        this.networkHandler = networkHandler;

        networkHandler.setOnMessageReceived(this::handleMessage);
    }

    private void handleMessage(TorrentMessage message) {
        if (message instanceof PingMessage) {
            handlePingMessage((PingMessage) message);
        } else if (message instanceof AckMessage) {
            handleAckMessage((AckMessage) message);
        } else if (message instanceof FileRequestMessage) {
            handleFileRequestMessage((FileRequestMessage) message);
        } else if (message instanceof FileDataMessage) {
            handleFileDataMessage((FileDataMessage) message);
        } else {
            log.warn("Received an unknown type of message.");
        }
    }

    private void handlePingMessage(PingMessage message) {
        AckMessage ack = new AckMessage(localNode.getAddress());
        networkHandler.sendMessageToNode(ack, message.getSenderAddress());
    }

    private void handleAckMessage(AckMessage message) {
        log.info("Acknowledgement received from " + message.getSenderAddress());
    }

    private void handleFileRequestMessage(FileRequestMessage message) {
        String filePath = localNode.getFromFileStore(message.getFileIdentifier());
        if (filePath == null) {
            log.warn("File not found:  " + message.getFileIdentifier());
            return;
        }

        String fileContent = readFileContent(filePath);
        if (fileContent != null) {
            FileDataMessage dataMessage = new FileDataMessage(message.getFileIdentifier(), fileContent);
            networkHandler.sendMessageToNode(dataMessage, message.getSenderAddress());
        } else {
            log.error("Error reading file: " + filePath);
        }
    }

    private void handleFileDataMessage(FileDataMessage message) {
        log.info("Received file data for " + message.getFileName() + ": " + message.getFileData());
    }

    private String readFileContent(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading file: " + e.getMessage());
            return null;
        }
    }

    public void downloadFile(String fileId, String sourceNodeId) {
        FileRequestMessage requestMessage = new FileRequestMessage(fileId, localNode.getAddress());
        networkHandler.sendMessageToNode(requestMessage, sourceNodeId);
    }

    public void pingNode(String address) {
        PingMessage pingMessage = new PingMessage(localNode.getAddress());
        networkHandler.sendMessageToNode(pingMessage, address);
    }
}
