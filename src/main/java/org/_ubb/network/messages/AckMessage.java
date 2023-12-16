package org._ubb.network.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ACK")
public class AckMessage extends KademliaMessage {
    private final String senderId;

    public AckMessage(String senderId) {
        super("ACK");
        this.senderId = senderId;
    }

    public String getSenderId() {
        return senderId;
    }
}
