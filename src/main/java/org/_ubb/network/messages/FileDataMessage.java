package org._ubb.network.messages;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDataMessage extends TorrentMessage {
    private String fileName;
    private String fileHash;
    private byte[] data;
    private int chunkIndex;
    private int totalChunks;
    private String md5Checksum;
    private String senderAddress;

    public FileDataMessage() {
        super("FILE_DATA");
    }

    public FileDataMessage(String fileName) {
        super("FILE_DATA");
        this.fileName = fileName;
    }

    public FileDataMessage(String fileName, String fileHash, byte[] data, int chunkIndex, int totalChunks, String checksum, String senderAddress) {
        super("FILE_DATA");
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.data = data;
        this.chunkIndex = chunkIndex;
        this.totalChunks = totalChunks;
        this.md5Checksum = checksum;
        this.senderAddress = senderAddress;
    }
}


