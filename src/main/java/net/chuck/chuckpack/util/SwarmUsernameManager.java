package net.chuck.chuckpack.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SwarmUsernameManager {

    private static final Map<String, String> cache = new ConcurrentHashMap<>();
    private static final Path USERNAME_FILE = Paths.get(
        System.getProperty("user.home"), ".ChuckPack", "swarm_usernames.txt"
    );

    public static synchronized String getOrCreateUsername(int workerId, String serverAddress) {
        String key = workerId + "@" + serverAddress.toLowerCase();
        if (cache.containsKey(key)) return cache.get(key);

        loadFromFile();

        if (cache.containsKey(key)) return cache.get(key);

        String username = generateUsername();
        cache.put(key, username);
        saveToFile();
        return username;
    }

    private static String generateUsername() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789_";
        Random random = new Random();
        int length = 8 + random.nextInt(6);
        StringBuilder sb = new StringBuilder();
        sb.append("_");
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static void loadFromFile() {
        if (!Files.exists(USERNAME_FILE)) return;
        try (BufferedReader reader = Files.newBufferedReader(USERNAME_FILE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                int eq = line.indexOf('=');
                if (eq > 0) {
                    cache.put(line.substring(0, eq), line.substring(eq + 1));
                }
            }
        } catch (IOException ignored) {}
    }

    private static void saveToFile() {
        try {
            Files.createDirectories(USERNAME_FILE.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(USERNAME_FILE)) {
                for (Map.Entry<String, String> entry : cache.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue());
                    writer.newLine();
                }
            }
        } catch (IOException ignored) {}
    }
}
