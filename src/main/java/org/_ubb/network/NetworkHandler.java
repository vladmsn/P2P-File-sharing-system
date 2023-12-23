package org._ubb.network;

import ch.qos.logback.classic.net.SimpleSocketServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org._ubb.network.messages.TorrentMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.net.URI;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;


@Slf4j
public class NetworkHandler {

    protected static final int RESPONSE_TIMEOUT_SECONDS = 10;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    private WebSocketServer server;
    @Getter
    private Consumer<TorrentMessage> onMessageReceived;
    @Getter
    private Map<WebSocket, String> nodeAddressMap = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, CompletableFuture<TorrentMessage>> pendingRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public NetworkHandler(int port) {
        startServer(port);
    }

    private void startServer(int port) {
        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                log.info("New connection to " + conn.getRemoteSocketAddress());
                nodeAddressMap.put(conn, conn.getRemoteSocketAddress().toString());
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                log.info("Connection closed to " + conn.getRemoteSocketAddress() + " with code " + code + " and reason " + reason);
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                String nodeAddress = nodeAddressMap.get(conn);
                log.info("Message from " + nodeAddress + ": " + message);

                try {
                    TorrentMessage receivedMessage = objectMapper.readValue(message, TorrentMessage.class);
                    CompletableFuture<TorrentMessage> future = pendingRequests.remove(receivedMessage.getId());
                    if (future != null) {
                        future.complete(receivedMessage);
                    }

                    if (onMessageReceived != null) {
                        onMessageReceived.accept(receivedMessage);
                    }
                } catch (JsonProcessingException e) {
                    log.error("Error in deserializing message: " + e.getMessage());
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                log.error("Error in connection to server: " + ex.getMessage());
            }

            @Override
            public void onStart() {
                log.info("Server started on port " + getPort());
            }
        };
        server.start();
    }


    public void sendMessageToNode(TorrentMessage message, String targetNodeAddress) {
        WebSocket targetConnection = findOrCreateConnection(targetNodeAddress);
        if (targetConnection != null) {
            sendMessageWhenConnected(message, targetConnection);
        }
    }

    protected WebSocket findOrCreateConnection(String nodeAddress) {
        Optional<WebSocket> existingConnection = nodeAddressMap.entrySet().stream()
                .filter(entry -> nodeAddress.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst();

        return existingConnection.orElseGet(() -> connectToServer(nodeAddress));
    }

    private WebSocket connectToServer(String nodeAddress) {
        try {
            URI serverUri = new URI("ws://" + nodeAddress);
            WebSocketClient newClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("New connection to " + getURI());
                    nodeAddressMap.put(this, nodeAddress);
                }

                @Override
                public void onMessage(String message) {
                    log.info("Message from " + getURI() + ": " + message);
                    try {
                        TorrentMessage receivedMessage = objectMapper.readValue(message, TorrentMessage.class);
                        if (onMessageReceived != null) {
                            onMessageReceived.accept(receivedMessage);
                        }
                    } catch (JsonProcessingException e) {
                        log.error("Error in deserializing message: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.info("Connection closed to " + getURI() + " with code " + code + " and reason " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    log.error("Error in connection to server: " + ex.getMessage());
                }
            };

            newClient.connectBlocking(); // Wait for the connection to be established
            return newClient;
        } catch (Exception e) {
            log.error("Error in connecting to server: " + e.getMessage());
            return null; // or handle differently
        }
    }

    private void sendMessageWhenConnected(TorrentMessage message, WebSocket conn) {
        CompletableFuture.runAsync(() -> {
            while (!conn.isOpen()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                conn.send(jsonMessage);

                CompletableFuture<TorrentMessage> future = new CompletableFuture<>();
                pendingRequests.put(message.getId(), future);

                scheduleTimeout(future, message.getId());
            } catch (JsonProcessingException e) {
                log.error("Error in serializing message: " + e.getMessage());
            }
        });
    }

    protected void scheduleTimeout(CompletableFuture<TorrentMessage> future, String messageId) {
        scheduler.schedule(() -> {
            CompletableFuture<TorrentMessage> pendingFuture = pendingRequests.remove(messageId);
            if (pendingFuture != null && !pendingFuture.isDone()) {
                pendingFuture.completeExceptionally(new TimeoutException("Response timed out"));
            }
        }, RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public void setOnMessageReceived(Consumer<TorrentMessage> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }
}