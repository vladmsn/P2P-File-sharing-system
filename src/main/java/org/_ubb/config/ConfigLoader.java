package org._ubb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class ConfigLoader {

    public NodeConfig loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.yml")) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(input, NodeConfig.class);
        } catch (Exception e) {
            log.error("An error occurred while reading configuration: ", e);
            return null;
        }
    }
}