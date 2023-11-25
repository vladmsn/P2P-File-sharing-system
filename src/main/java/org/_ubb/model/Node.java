package org._ubb.model;

import lombok.Data;
import lombok.Getter;
import org._ubb.utils.EncryptionUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Node {
    private final String identifier;
    private final List<KBucket> routingTable;
    private final Map<String, String> dataStore;

    public Node(String host, int port) {
        this.identifier = EncryptionUtils.generateNodeIdentifier(host, port);
        this.dataStore = new HashMap<>();

        this.routingTable = new ArrayList<>(160);
        for (int i = 0; i < 160; i++) {
            routingTable.add(new KBucket());
        }
    }

    public int getBucketIndex(String nodeId) {
        BigInteger distance = calculateDistance(this.identifier, nodeId);
        if (distance.equals(BigInteger.ZERO)) {
            return -1;
        }

        int highestBit = distance.bitLength() - 1;

        return 159 - highestBit;
    }

    private BigInteger calculateDistance(String nodeId1, String nodeId2) {
        BigInteger id1 = new BigInteger(nodeId1, 16);
        BigInteger id2 = new BigInteger(nodeId2, 16);
        BigInteger xorDistance = id1.xor(id2);

        // Ensure the distance is within a 160-bit space
        BigInteger maxDistance = new BigInteger("1").shiftLeft(160).subtract(BigInteger.ONE);
        return xorDistance.and(maxDistance);
    }

    public void addNodeToRoutingTable(String nodeId, String connectionDetails) {
        int bucketIndex = getBucketIndex(nodeId);
        routingTable.get(bucketIndex).addNode(nodeId, connectionDetails);
    }

    public void removeNodeFromRoutingTable(String nodeId) {
        int bucketIndex = getBucketIndex(nodeId);
        routingTable.get(bucketIndex).removeNode(nodeId);
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

}