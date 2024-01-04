package org._ubb.network.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PingMessage extends TorrentMessage {
    private  String senderAddress;

    public PingMessage() {
        super("PING");
    }

    public PingMessage(String senderAddress) {
        super("PING");
        this.senderAddress = senderAddress;
    }

}
