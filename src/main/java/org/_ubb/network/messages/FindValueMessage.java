package org._ubb.network.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("FIND_VALUE")
public class FindValueMessage extends KademliaMessage {
    private final String key;

    public FindValueMessage(String key) {
        super("FIND_VALUE");
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
