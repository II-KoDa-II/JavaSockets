package org.example;

import java.io.IOException;
import java.util.Properties;

public class PropertyLoader {
    private final Properties properties;

    public PropertyLoader(String fileName) {
        properties = new Properties();
        try {
            properties.load(PropertyLoader.class.getClassLoader().getResourceAsStream(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from file: " + fileName, e);
        }
    }

    public String getHost(){
        return properties.getProperty("server.host");
    }

    public String getPort(){
        return properties.getProperty("server.port");
    }
}