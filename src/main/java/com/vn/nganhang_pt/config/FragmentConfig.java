package com.vn.nganhang_pt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cấu hình các phân mảnh database từ application.yml
 * Dùng List thay vì Map để tránh vấn đề với backslash trong key
 */
@Configuration
@ConfigurationProperties(prefix = "app.fragments")
public class FragmentConfig {

    public static class Server {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    private String username;
    private String password;
    private List<Server> servers = new ArrayList<>();
    private Map<String, String> connections = new HashMap<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
        // Convert list to map for easy lookup
        this.connections.clear();
        for (Server server : servers) {
            connections.put(server.getName(), server.getUrl());
        }
        // Debug
        System.out.println("[DEBUG] FragmentConfig loaded " + servers.size() + " servers:");
        servers.forEach(server -> {
            System.out.println("[DEBUG]   Name: [" + server.getName() + "]");
            System.out.println("[DEBUG]   Length: " + server.getName().length() + ", contains backslash: "
                    + server.getName().contains("\\"));
            System.out.println(
                    "[DEBUG]   URL: " + server.getUrl().substring(0, Math.min(60, server.getUrl().length())) + "...");
        });
    }

    public Map<String, String> getConnections() {
        return connections;
    }

    /**
     * Lấy connection string theo tên server
     */
    public String getConnectionString(String serverName) {
        return connections.get(serverName);
    }
}
