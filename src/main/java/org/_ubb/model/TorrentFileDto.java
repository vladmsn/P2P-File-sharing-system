package org._ubb.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TorrentFileDto {
    private UUID peerIdentifier;
    private String fileHash;
    private String fileName;
    private String fileType;
    private int fileSize;
    private List<PeerDto> activePeers;

    // Convert DTO to JSON
    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    // Convert JSON to DTO
    public static TorrentFileDto fromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, TorrentFileDto.class);
    }

    @Override
    public String toString() {
        String format = "| %-15s | %-30s | %-10s | %-10s | %-10s | %-30s |%n";
        return String.format(format,
                peerIdentifier,
                fileHash,
                fileName,
                fileType,
                fileSize,
                activePeers.size() + " peers");
    }
}
