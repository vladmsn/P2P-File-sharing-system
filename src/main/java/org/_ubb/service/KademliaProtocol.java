package org._ubb.service;

import lombok.extern.slf4j.Slf4j;
import org._ubb.model.KademliaNode;
import org._ubb.network.NetworkHandler;
import org._ubb.network.messages.AckMessage;
import org._ubb.network.messages.FindNodeMessage;
import org._ubb.network.messages.FindValueMessage;
import org._ubb.network.messages.KademliaMessage;
import org._ubb.network.messages.PingMessage;
import org._ubb.network.messages.StoreMessage;

@Slf4j
public class KademliaProtocol {

    private final KademliaNode localNode;
    private final NetworkHandler networkHandler;

    public KademliaProtocol(KademliaNode localNode, NetworkHandler networkHandler) {
        this.localNode = localNode;
        this.networkHandler = networkHandler;

        networkHandler.setOnMessageReceived(this::handleMessage);
    }

    private void handleMessage(KademliaMessage message) {
        if (message instanceof PingMessage) {
            handlePingMessage((PingMessage) message);
        } else if (message instanceof StoreMessage) {
            handleStoreMessage((StoreMessage) message);
        } else if (message instanceof FindNodeMessage) {
            handleFindNodeMessage((FindNodeMessage) message);
        } else if (message instanceof FindValueMessage) {
            handleFindValueMessage((FindValueMessage) message);
        } else if (message instanceof AckMessage) {
            handleAckMessage((AckMessage) message);
        }
        else {
           log.warn("Received an unknown type of message.");
        }
    }

    private void handleAckMessage(AckMessage message) {
    }

    private void handlePingMessage(PingMessage message) {
        // Implementation for handling PingMessage
    }

    private void handleStoreMessage(StoreMessage message) {
        // Implementation for handling StoreMessage
    }

    private void handleFindNodeMessage(FindNodeMessage message) {
        // Implementation for handling FindNodeMessage
    }

    private void handleFindValueMessage(FindValueMessage message) {
        // Implementation for handling FindValueMessage
    }

    public void sendPing(String targetAddress) {
        PingMessage ping = new PingMessage(localNode.getIdentifier());
        networkHandler.sendMessageToNode(ping, targetAddress);
    }

    public void sendStore(String targetAddress, String key, String value) {
        StoreMessage store = new StoreMessage(key, value);
        networkHandler.sendMessageToNode(store, targetAddress);
    }

    public void sendFindNode(String targetAddress, String nodeId) {
        FindNodeMessage findNode = new FindNodeMessage(nodeId);
        networkHandler.sendMessageToNode(findNode, targetAddress);
    }

    public void sendFindValue(String targetAddress, String key) {
        FindValueMessage findValue = new FindValueMessage(key);
        networkHandler.sendMessageToNode(findValue, targetAddress);
    }
}
