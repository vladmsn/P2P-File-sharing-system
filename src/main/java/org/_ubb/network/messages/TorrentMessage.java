package org._ubb.network.messages;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public abstract class TorrentMessage {
    private final String id;
    private final String type;

    public TorrentMessage() {
        this.id = java.util.UUID.randomUUID().toString();
        this.type = this.getClass().getSimpleName();
    }

    public TorrentMessage(String type) {
        this.id = java.util.UUID.randomUUID().toString();
        this.type = type;
    }
}
