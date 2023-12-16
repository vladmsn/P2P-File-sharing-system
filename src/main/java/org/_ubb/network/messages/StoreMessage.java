package org._ubb.network.messages;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("STORE")
public class StoreMessage extends KademliaMessage {
    private final String key;
    private final String value;

    public StoreMessage(String key, String value) {
        super("STORE");
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}

