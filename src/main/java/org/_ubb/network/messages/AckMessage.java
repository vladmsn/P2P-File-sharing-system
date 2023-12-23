package org._ubb.network.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonTypeName("ACK")
public class AckMessage extends TorrentMessage {
    private String senderId;

    public AckMessage() {
        super("ACK");
    }

    public AckMessage(String senderId) {
        super("ACK");
        this.senderId = senderId;
    }

}
