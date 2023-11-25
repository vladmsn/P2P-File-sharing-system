package org._ubb.model;

import java.util.LinkedHashMap;
import java.util.Map;


public class KBucket {
    private static final int K = 20;
    private final LinkedHashMap<String, String> nodes;

    public KBucket() {
        this.nodes = new LinkedHashMap<String, String>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > K; // This ensures the k-bucket size doesn't exceed K
            }
        };
    }

    public void addNode(String nodeId, String connectionDetails) {
        nodes.remove(nodeId);
        nodes.put(nodeId, connectionDetails); // Add to the end (most recently seen)
    }

    public void removeNode(String nodeId) {
        nodes.remove(nodeId);
    }

    public String findNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public boolean containsNode(String nodeId) {
        return nodes.containsKey(nodeId);
    }
}
