package org._ubb.network.messages;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PingMessage.class, name = "PING"),
        @JsonSubTypes.Type(value = StoreMessage.class, name = "STORE"),
        @JsonSubTypes.Type(value = FindNodeMessage.class, name = "FIND_NODE"),
        @JsonSubTypes.Type(value = FindValueMessage.class, name = "FIND_VALUE"),
        @JsonSubTypes.Type(value = AckMessage.class, name = "ACK"),
})
public abstract class KademliaMessage {
    private final String id;
    private final String type;

    public KademliaMessage(String type) {
        this.id = java.util.UUID.randomUUID().toString();
        this.type = type;
    }
}
