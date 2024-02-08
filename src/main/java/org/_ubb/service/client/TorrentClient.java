package org._ubb.service.client;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org._ubb.config.TorrentClientConfig;
import org._ubb.model.PeerDto;
import org._ubb.model.TorrentFileDto;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TorrentClient {
    private final static String PEERS_API = "/api/v1/peers";
    private final static String PEERS_REGISTER_API = PEERS_API + "/register";
    private final static String FILES_API = "/api/v1/files";
    private final static String FILES_PEERS_API = FILES_API +  "/{fileHash}/getPeers";

    private final HttpClient httpClient;
    private final TorrentClientConfig torrentClientConfig;


    public TorrentClient(TorrentClientConfig torrentClientConfig) {
        this.torrentClientConfig = torrentClientConfig;
        this.httpClient = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(torrentClientConfig.getTimeout())
                        .build();
    }

    public UUID registerPeer() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(torrentClientConfig.getUrl() + PEERS_REGISTER_API))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();


        log.info("Registering peer with request: " + request);

        HttpResponse<String> response = sendRequestWithRetry(request);

        if (response.statusCode() == 200) {
            String trimmedName = response.body().trim().replaceAll("\"", "");
            log.info("Response body (trimmed): " + trimmedName);
            return UUID.fromString(trimmedName);
        } else {
            throw new RuntimeException("Failed to register peer: " + response.body());
        }
    }

    public void addFile(UUID peerIdentifier, String fileHash, String fileName, String fileType, int fileSize)
            throws IOException, InterruptedException {
        TorrentFileDto file = TorrentFileDto.builder()
                .peerIdentifier(peerIdentifier)
                .fileHash(fileHash)
                .fileName(fileName)
                .fileType(fileType)
                .fileSize(fileSize)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(torrentClientConfig.getUrl() + FILES_API + "/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(file.toJson()))
                .build();

        HttpResponse<String> response = sendRequestWithRetry(request);

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to add file: " + response.body());
        }
    }

    public List<TorrentFileDto> getFiles() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(torrentClientConfig.getUrl() + FILES_API + "/"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info(response.body());

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), new TypeReference<List<TorrentFileDto>>(){});
        } else {
            throw new RuntimeException("Failed to fetch files: " + response.body());
        }
    }

    public List<PeerDto> getPeers(String fileHash) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(torrentClientConfig.getUrl() + FILES_PEERS_API.replace("{fileHash}", fileHash)))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info(response.body());
        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), new TypeReference<>(){});
        } else {
            throw new RuntimeException("Failed to fetch peers for file hash: " + fileHash + " - " + response.body());
        }
    }

    private HttpResponse<String> sendRequestWithRetry(HttpRequest request) throws InterruptedException, IOException {
        int retryCount = 0;
        final long initialBackoff = 1000L;
        IOException lastException = new IOException();

        while (retryCount < torrentClientConfig.getMaxRetryCount()) {
            try {
                return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                lastException = e;

                if (e instanceof HttpTimeoutException || e instanceof ConnectException) {
                    retryCount++;
                    System.out.println("Attempt " + retryCount + " failed with " + e.getClass().getSimpleName() + ", retrying...");
                    long backoff = calculateBackoff(initialBackoff, retryCount);
                    Thread.sleep(backoff);
                } else {
                    throw e;
                }
            }
        }

        throw new IOException("Failed after " + retryCount + " retries", lastException);
    }

    /**
     * Calculates the backoff time. This implementation uses exponential backoff with a cap.
     *
     * @param initialBackoff The initial backoff time in milliseconds.
     * @param retryCount The current retry attempt.
     * @return The backoff time in milliseconds.
     */
    private long calculateBackoff(long initialBackoff, int retryCount) {
        long backoff = (long) (initialBackoff * Math.pow(2, retryCount - 1));
        long maxBackoff = 30000L; // Maximum backoff time cap in milliseconds
        return Math.min(backoff, maxBackoff);
    }
}
