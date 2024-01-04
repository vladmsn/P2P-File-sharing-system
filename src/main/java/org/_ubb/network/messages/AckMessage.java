package org._ubb.network.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AckMessage extends TorrentMessage {
    private String senderAddress;

    public AckMessage() {
        super("ACK");
    }

    public AckMessage(String senderId) {
        super("ACK");
        this.senderAddress = senderAddress;
    }

}
