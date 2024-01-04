package org._ubb.network.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDataMessage extends TorrentMessage {
    private String fileName;
    private String fileData;  // For simplicity, using String to represent file data

    public FileDataMessage() {
        super("FILE_DATA");
    }

    public FileDataMessage(String fileName, String fileData) {
        super("FILE_DATA");
        this.fileName = fileName;
        this.fileData = fileData;
    }
}


