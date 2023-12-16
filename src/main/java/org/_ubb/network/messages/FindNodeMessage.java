package org._ubb.network.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("FIND_NODE")
public class FindNodeMessage extends KademliaMessage {
    private final String targetNodeId;

    public FindNodeMessage(String targetNodeId) {
        super("FIND_NODE");
        this.targetNodeId = targetNodeId;
    }

    public String getTargetNodeId() {
        return targetNodeId;
    }
}
