package org._ubb.config;

import lombok.Data;

@Data
public class NodeConfig {
    private String bootstrapNodeAddress;
    private int localNodePort;
}