package com.example.nettyhttp.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GatewayConfig {
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_CONFIG_PATH = "config/gateway.properties";

    private final int port;
    private final List<Route> routes;

    public GatewayConfig(int port, List<Route> routes) {
        this.port = port;
        this.routes = List.copyOf(routes);
    }

    public static GatewayConfig load() {
        Path path = Path.of(configPath());
        Properties properties = new Properties();

        if (Files.exists(path)) {
            try (InputStream inputStream = Files.newInputStream(path)) {
                properties.load(inputStream);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to load gateway config: " + path.toAbsolutePath(), exception);
            }
        } else {
            System.out.println("Gateway config not found, using defaults: " + path.toAbsolutePath());
        }

        return new GatewayConfig(readPort(properties), readRoutes(properties));
    }

    private static String configPath() {
        String systemProperty = System.getProperty("gateway.config");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String environmentValue = System.getenv("GATEWAY_CONFIG");
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }

        return DEFAULT_CONFIG_PATH;
    }

    private static int readPort(Properties properties) {
        String value = properties.getProperty("server.port");
        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("server.port must be a valid integer: " + value, exception);
        }
    }

    private static List<Route> readRoutes(Properties properties) {
        List<Route> routes = new ArrayList<>();

        for (int index = 0; ; index++) {
            String prefix = properties.getProperty("routes." + index + ".pathPrefix");
            String target = properties.getProperty("routes." + index + ".target");

            if (prefix == null && target == null) {
                break;
            }

            if (prefix == null || prefix.isBlank() || target == null || target.isBlank()) {
                throw new IllegalArgumentException("Route " + index + " requires pathPrefix and target");
            }

            String id = properties.getProperty("routes." + index + ".id", "route-" + index);
            boolean stripPrefix = Boolean.parseBoolean(properties.getProperty("routes." + index + ".stripPrefix", "false"));
            routes.add(new Route(id.trim(), normalizePrefix(prefix), target.trim(), stripPrefix));
        }

        return routes;
    }

    private static String normalizePrefix(String value) {
        String prefix = value.trim();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (prefix.length() > 1 && prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }

    public int port() {
        return port;
    }

    public List<Route> routes() {
        return routes;
    }
}

