package org._ubb.service;

import lombok.extern.slf4j.Slf4j;
import org._ubb.model.ClientNode;
import org._ubb.model.TorrentFileDto;
import org._ubb.network.NetworkHandler;
import org._ubb.network.messages.AckMessage;
import org._ubb.network.messages.FileDataMessage;
import org._ubb.network.messages.FileRequestMessage;
import org._ubb.network.messages.PingMessage;
import org._ubb.network.messages.TorrentMessage;
import org._ubb.service.client.TorrentClient;
import org._ubb.utils.MD5;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org._ubb.utils.FileUtils.calculateTotalChunks;
import static org._ubb.utils.FileUtils.readFileChunk;


@Slf4j
public class FileTransferService {

    private final ClientNode localNode;
    private final NetworkHandler networkHandler;
    private final TorrentClient torrentClient;
    private final Map<String, List<FileDataMessage>> fileChunksMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> retryCountMap = new ConcurrentHashMap<>();

    public FileTransferService(ClientNode localNode, NetworkHandler networkHandler, TorrentClient torrentClient) {
        this.localNode = localNode;
        this.networkHandler = networkHandler;
        this.torrentClient = torrentClient;

        networkHandler.setOnMessageReceived(this::handleMessage);
    }

    public void connectToNetwork() {
        try {
            this.localNode.setClientId(torrentClient.registerPeer());
            System.out.println("Successfully connected to the network. Peer ID: " + localNode.getClientId());
        } catch (Exception e) {
            System.out.println("Failed to connect to the network: " + e.getMessage());
        }
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

        int CHUNK_SIZE = 1024 * 1024;

        try {
            File file = new File(filePath);
            int totalChunks = calculateTotalChunks(file.length(), CHUNK_SIZE);

            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            String md5checksum = MD5.md5ToHexString(fileContent);

            for (int i = 0; i < totalChunks; i++) {
                byte[] chunkData = readFileChunk(filePath, CHUNK_SIZE, i);
                FileDataMessage dataMessage = FileDataMessage.builder()
                        .fileName(file.getName())
                        .fileHash(message.getFileIdentifier())
                        .data(chunkData)
                        .chunkIndex(i)
                        .totalChunks(totalChunks)
                        .md5Checksum(md5checksum)
                        .senderAddress(localNode.getAddress())
                        .build();

                networkHandler.sendMessageToNode(dataMessage, message.getSenderAddress());
            }
        } catch (IOException e) {
            log.error("Error processing file request: " + e.getMessage());
        }
    }

    private void handleFileDataMessage(FileDataMessage message) {
        log.info("Received file data for " + message.getFileName() + ", Chunk: " + message.getChunkIndex());

        fileChunksMap.computeIfAbsent(message.getFileName(), k -> new ArrayList<>()).add(message);

        if (hasReceivedAllChunks(message.getFileName(), message.getTotalChunks())) {
            try {
                reconstructFile(message.getFileName(), message.getMd5Checksum());

                Path path = Paths.get("/downloads/" + message.getFileName());
                String fileType = Files.probeContentType(path);
                long fileSizeInBytes = Files.size(path);

                torrentClient.addFile(localNode.getClientId(), message.getFileHash(), message.getFileName(), fileType, (int) (fileSizeInBytes / (1024 * 1024)));
            } catch (SecurityException e) {
                log.error("Security exception while reconstructing file: " + message.getFileName(), e);

                if (retryCountMap.computeIfAbsent(message.getFileName(), k -> new AtomicInteger(0)).incrementAndGet() < 3) {
                    log.info("Requesting file again: " + message.getFileName());
                    networkHandler.sendMessageToNode(new AckMessage(localNode.getAddress()), message.getSenderAddress());
                } else {
                    log.error("Max retries reached for file: " + message.getFileName());
                }
            }
            catch (IOException | NoSuchAlgorithmException e) {
                log.error("Error reconstructing file: " + message.getFileName(), e);
            } catch (InterruptedException e) {
                log.error("Error adding file to the network: " + message.getFileName(), e);
            }
        }
    }

    private boolean hasReceivedAllChunks(String fileName, int totalChunks) {
        List<FileDataMessage> chunks = fileChunksMap.get(fileName);
        return chunks != null && chunks.size() == totalChunks;
    }

    private void reconstructFile(String fileName, String expectedChecksum) throws IOException, NoSuchAlgorithmException {
        List<FileDataMessage> chunks = fileChunksMap.get(fileName);
        if (chunks == null) {
            throw new FileNotFoundException("File chunks not found for " + fileName);
        }
        chunks.sort(Comparator.comparingInt(FileDataMessage::getChunkIndex));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (FileDataMessage chunk : chunks) {
            baos.write(chunk.getData());
        }

        String directoryPath = "/downloads";
        try (FileOutputStream fileOutputStream = new FileOutputStream(directoryPath + fileName)) {
            baos.writeTo(fileOutputStream);
        }

        String actualChecksum = MD5.md5ToHexString(baos.toByteArray());

        if (!expectedChecksum.equalsIgnoreCase(actualChecksum)) {
            log.error("Checksum mismatch for file: " + fileName);
            throw new SecurityException("Checksum mismatch for file: " + fileName);
        }

        fileChunksMap.remove(fileName);
        log.info("File reconstructed successfully with a matching checksum: " + fileName);
    }

    public void downloadFile(String fileId) throws IOException, InterruptedException {
        FileRequestMessage requestMessage = new FileRequestMessage(fileId, localNode.getAddress());

        this.torrentClient.getPeers(fileId)
                .stream()
                .findFirst()
                .ifPresentOrElse(peer -> networkHandler.sendMessageToNode(requestMessage, peer.getAddress()),
                        () -> log.error("No peers found for file: " + fileId));
    }

    public List<TorrentFileDto> getFiles() throws IOException, InterruptedException {
        return this.torrentClient.getFiles();
    }

    public void pingNode(String address) {
        PingMessage pingMessage = new PingMessage(localNode.getAddress());
        networkHandler.sendMessageToNode(pingMessage, address);
    }

    public void uploadFile(String identifier, String filepath) throws IOException, InterruptedException {
        Path path = Paths.get(filepath);
        String fileType = Files.probeContentType(path);
        long fileSizeInBytes = Files.size(path);
        int fileSizeInMB = (int) (fileSizeInBytes / (1024 * 1024));

        this.torrentClient.addFile(localNode.getClientId(), identifier, new File(filepath).getName(), fileType, fileSizeInMB);
    }
}
