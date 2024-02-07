package org._ubb.config;

import lombok.Data;

import java.time.Duration;

@Data
public class TorrentClientConfig {
    private String url = "http://localhost:1237";
    private Duration timeout = Duration.ofSeconds(5);
    private Integer maxRetryCount = 3;
}
