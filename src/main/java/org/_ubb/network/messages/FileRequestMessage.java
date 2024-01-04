package org._ubb.network.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileRequestMessage extends TorrentMessage {
    private String fileIdentifier;
    private String senderAddress;

    public FileRequestMessage() {
        super("FILE_REQUEST");
    }

    public FileRequestMessage(String fileIdentifier, String senderAddress) {
        super("FILE_REQUEST");
        this.fileIdentifier = fileIdentifier;
        this.senderAddress = senderAddress;
    }
}
