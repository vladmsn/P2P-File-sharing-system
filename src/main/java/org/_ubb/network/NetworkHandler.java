package org._ubb.network;

import org._ubb.network.messages.KademliaMessage;

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


public class NetworkHandler {

    private static final int RESPONSE_TIMEOUT_SECONDS = 10;
    private final ObjectMapper objectMapper = new ObjectMapper();


    private WebSocketServer server;
    private Map<WebSocket, String> nodeAddressMap = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<KademliaMessage>> pendingRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Consumer<KademliaMessage> onMessageReceived;

    public NetworkHandler(int port) {
        startServer(port);
    }

    private void startServer(int port) {
        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("New connection from " + conn.getRemoteSocketAddress());
                // Extract node information from handshake or initial message
                // For simplicity, let's assume the remote socket address is the node address
                nodeAddressMap.put(conn, conn.getRemoteSocketAddress().toString());            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("Closed connection to " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                System.out.println("Message from " + conn.getRemoteSocketAddress() + ": " + message);
                // Handle the message
                String nodeAddress = nodeAddressMap.get(conn);

                try {
                    KademliaMessage receivedMessage = objectMapper.readValue(message, KademliaMessage.class);
                    CompletableFuture<KademliaMessage> future = pendingRequests.remove(receivedMessage.getId());
                    if (future != null) {
                        future.complete(receivedMessage);
                    }

                    if (onMessageReceived != null) {
                        onMessageReceived.accept(receivedMessage);
                    }
                } catch (JsonProcessingException e) {
                    System.err.println("Error in deserializing message: " + e.getMessage());
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }

            @Override
            public void onStart() {
                System.out.println("Server started successfully");
            }
        };
        server.start();
    }


    public void sendMessageToNode(KademliaMessage message, String targetNodeAddress) {
        WebSocket targetConnection = findOrCreateConnection(targetNodeAddress);
        if (targetConnection != null) {
            sendMessageWhenConnected(message, targetConnection);
        }
    }

    private WebSocket findOrCreateConnection(String nodeAddress) {
        Optional<WebSocket> existingConnection = nodeAddressMap.entrySet().stream()
                .filter(entry -> nodeAddress.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst();

        return existingConnection.orElseGet(() -> connectToServer(nodeAddress));
    }

    private WebSocket connectToServer(String nodeAddress) {
        try {
            // Assuming nodeAddress is in the format "host:port"
            URI serverUri = new URI("ws://" + nodeAddress);
            WebSocketClient newClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("New connection opened to " + getURI());
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("Received message: " + message);
                    try {
                        KademliaMessage receivedMessage = objectMapper.readValue(message, KademliaMessage.class);
                        if (onMessageReceived != null) {
                            onMessageReceived.accept(receivedMessage);
                        }
                    } catch (JsonProcessingException e) {
                        System.err.println("Error in deserializing message: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Closed connection to " + getURI());
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("Error on connection to " + getURI() + ": " + ex.getMessage());
                }
            };

            newClient.connectBlocking(); // Wait for the connection to be established
            return newClient;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // or handle differently
        }
    }

    private void sendMessageWhenConnected(KademliaMessage message, WebSocket conn) {
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

                CompletableFuture<KademliaMessage> future = new CompletableFuture<>();
                pendingRequests.put(message.getId(), future);

                scheduleTimeout(future, message.getId());
            } catch (JsonProcessingException e) {
                System.err.println("Error in serializing message: " + e.getMessage());
            }
        });
    }

    private void scheduleTimeout(CompletableFuture<KademliaMessage> future, String messageId) {
        scheduler.schedule(() -> {
            CompletableFuture<KademliaMessage> pendingFuture = pendingRequests.remove(messageId);
            if (pendingFuture != null && !pendingFuture.isDone()) {
                pendingFuture.completeExceptionally(new TimeoutException("Response timed out"));
            }
        }, RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public void setOnMessageReceived(Consumer<KademliaMessage> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public CompletableFuture<KademliaMessage> getFutureForMessage(String messageId) {
        return pendingRequests.get(messageId);
    }

}