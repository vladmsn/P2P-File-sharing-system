package org._ubb.model;

import lombok.Data;
import org._ubb.utils.EncryptionUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class ClientNode {

    private UUID clientId;
    private final String host;
    private final int port;
    private final Map<String, String> dataStore;

    public ClientNode(String host, int port) {
        this.host = host;
        this.port = port;
        this.dataStore = new HashMap<>();
    }


    public void addToDataStore(String fileId, String filePath) {
        dataStore.put(fileId, filePath);
    }

    public String getFromFileStore(String fileId) {
        return dataStore.get(fileId);
    }

    public void removeFromDataStore(String fileId) {
        dataStore.remove(fileId);
    }

    public String getAddress() {
        return host + ":" + port;
    }
}