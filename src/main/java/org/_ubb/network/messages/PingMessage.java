package org._ubb.network.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

@Getter
@JsonTypeName("PING")
public class PingMessage extends KademliaMessage {
    private final String senderId;

    public PingMessage(String senderId) {
        super("PING");
        this.senderId = senderId;
    }

}
