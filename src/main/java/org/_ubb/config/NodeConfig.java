package org._ubb.config;

import lombok.Data;

@Data
public class NodeConfig {
    private String bootstrapNodeAddress = "localhost:8080";
    private int localNodePort = 1234;
}