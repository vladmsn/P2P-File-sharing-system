package org._ubb.network.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonTypeName("PING")
public class PingMessage extends TorrentMessage {
    private  String senderId;

    public PingMessage() {
        super("PING");
    }

    public PingMessage(String senderId) {
        super("PING");
        this.senderId = senderId;
    }

}
