package org._ubb.network.messages;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PingMessage.class, name = "PING"),
        @JsonSubTypes.Type(value = AckMessage.class, name = "ACK"),
        @JsonSubTypes.Type(value = FileRequestMessage.class, name = "FILE_REQUEST"),
        @JsonSubTypes.Type(value = FileDataMessage.class, name = "FILE_DATA")
})
public abstract class TorrentMessage {
    private final String id;
    private final String type;

    public TorrentMessage() {
        this.id = java.util.UUID.randomUUID().toString();
        this.type = "UNKNOWN";
    }

    public TorrentMessage(String type) {
        this.id = java.util.UUID.randomUUID().toString();
        this.type = type;
    }
}
