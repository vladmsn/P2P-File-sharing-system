//package org._ubb.network;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import org._ubb.network.messages.TorrentMessage;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.InjectMocks;
//
//import static org._ubb.network.NetworkHandler.RESPONSE_TIMEOUT_SECONDS;
//import static org.awaitility.Awaitility.await;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.lenient;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.timeout;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.WebSocket;
//
//@ExtendWith(MockitoExtension.class)
//public class NetworkHandlerTest {
//
//    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
//    @Mock
//    private WebSocket mockWebSocket;
//    @Mock
//    private WebSocketClient mockWebSocketClient;
//    @InjectMocks
//    private NetworkHandler networkHandler = new NetworkHandler(8080);
//
//    @BeforeEach
//    void setUp() throws Exception {
//        lenient().doNothing().when(mockWebSocket).send(any(String.class));
//        lenient().when(mockWebSocket.isOpen()).thenReturn(true);
//        lenient().doAnswer(invocation -> null).when(mockWebSocketClient).connectBlocking();
//    }
//
//    @AfterEach
//    void tearDown() throws InterruptedException {
//        if (networkHandler.getServer() != null) {
//            networkHandler.getServer().stop();
//        }
//    }
//
//    @Test
//    void testSendMessageToNode_existingConnection() throws Exception {
//        TorrentMessage mockMessage = new TorrentMessage("testType") {};
//        String targetNodeAddress = "localhost:8080";
//
//        networkHandler.getNodeAddressMap().put(mockWebSocket, targetNodeAddress);
//
//        networkHandler.sendMessageToNode(mockMessage, targetNodeAddress);
//
//        verify(mockWebSocket, timeout(1000)).send(any(String.class));
//    }
//
//
//    @Test
//    void testSendMessageToNode_newConnection() throws Exception {
//        TorrentMessage mockMessage = new TorrentMessage("testType") {};
//        String targetNodeAddress = "localhost:8080";
//
//        networkHandler.sendMessageToNode(mockMessage, targetNodeAddress);
//
//        verify(mockWebSocketClient, timeout(1000)).send(any(String.class));
//    }
//
//    @Test
//    void testFindOrCreateConnection_existingConnection() {
//        String nodeAddress = "localhost:8080";
//        networkHandler.getNodeAddressMap().put(mockWebSocket, nodeAddress);
//
//        WebSocket result = networkHandler.findOrCreateConnection(nodeAddress);
//
//        assertEquals(mockWebSocket, result);
//    }
//
//    @Test
//    void testFindOrCreateConnection_newConnection() {
//        String nodeAddress = "localhost:8080";
//        WebSocket result = networkHandler.findOrCreateConnection(nodeAddress);
//
//        assertNotNull(result);
//    }
//
//    @Test
//    void testSendMessageWhenConnected_error() throws JsonProcessingException {
//        TorrentMessage mockMessage = new TorrentMessage("testType") {};
//        when(mockWebSocket.isOpen()).thenReturn(true);
//        doThrow(new JsonProcessingException("Serialization error") {}).when(objectMapper).writeValueAsString(mockMessage);
//
//        networkHandler.sendMessageToNode(mockMessage, "localhost:8080");
//    }
//
//    @Test
//    void testScheduleTimeout() {
//        String messageId = "testMessageId";
//        CompletableFuture<TorrentMessage> future = new CompletableFuture<>();
//        networkHandler.getPendingRequests().put(messageId, future);
//
//        networkHandler.scheduleTimeout(future, messageId);
//
//        await().atMost(RESPONSE_TIMEOUT_SECONDS + 1, TimeUnit.SECONDS).untilAsserted(() -> assertTrue(future.isCompletedExceptionally()));
//
//        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
//        assertTrue(exception.getCause() instanceof TimeoutException);
//    }
//}
