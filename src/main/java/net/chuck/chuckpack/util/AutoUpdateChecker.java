package net.chuck.chuckpack.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.chuck.chuckpack.ChuckPack;
import net.chuck.chuckpack.util.config.ChuckPackConfigModifier;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class AutoUpdateChecker {
    private static String latestVersion = "";
    private static String downloadUrl = "";
    private static String latestFileName = "";
    private static boolean updateAvailable = false;
    public static String splashStatus = "";

    public static void checkOnStartup() {
        ChuckPackConfigModifier cfg = ChuckPackConfigModifier.get();
        if (!cfg.checkForUpdates.get()) {
            ChuckPack.LOG.info("AutoUpdate: check on startup disabled.");
            return;
        }
        checkForUpdates(cfg.autoDownloadUpdates.get());
    }

    public static void checkForUpdates(boolean autoDownload) {
        CompletableFuture.runAsync(() -> {
            try {
                String currentVersion = getVersion();
                HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/chuck121110-bit/Chuck-Pack/releases/latest"))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "ChuckPack-AutoUpdater")
                    .GET()
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    latestVersion = json.get("tag_name").getAsString().replace("v", "").trim();

                    String currentClean = currentVersion.trim();
                    String latestClean = latestVersion.trim();

                    if (isNewerVersion(latestClean, currentClean)) {
                        updateAvailable = true;
                        splashStatus = "Chuck Pack has an update: " + currentClean + " -> " + latestClean;
                        ChuckPack.LOG.info("Update available: {} -> {}", currentClean, latestClean);

                        if (json.has("assets")) {
                            for (var asset : json.getAsJsonArray("assets")) {
                                JsonObject assetObj = asset.getAsJsonObject();
                                String name = assetObj.get("name").getAsString();
                                String lower = name.toLowerCase();
                                if (name.endsWith(".jar") && lower.contains("chuck") && lower.contains("pack")) {
                                    downloadUrl = assetObj.get("browser_download_url").getAsString();
                                    latestFileName = name;
                                    ChuckPack.LOG.info("Download: {} (file: {})", downloadUrl, latestFileName);
                                    break;
                                }
                            }
                        }

                        if (autoDownload && !downloadUrl.isEmpty()) {
                            splashStatus = "Chuck Pack downloading update: " + latestClean;
                            downloadUpdate();
                        } else {
                            ChuckPack.LOG.info("Open Config > Chuck Pack or check GitHub Releases to update. URL: {}", downloadUrl);
                        }
                    } else {
                        updateAvailable = false;
                        splashStatus = "Chuck Pack up to date: " + currentClean;
                        ChuckPack.LOG.info("Chuck Pack is up to date: {}", currentClean);
                    }
                } else if (response.statusCode() == 404) {
                    // No releases yet or repo private/network blocked on Linux Chromebook (qnd8U8h) — not an error
                    splashStatus = "Chuck Pack up to date (no releases)";
                    ChuckPack.LOG.info("AutoUpdate: no releases found (404) — you are on {}", currentVersion);
                } else {
                    splashStatus = "Chuck Pack update check failed: " + response.statusCode();
                    ChuckPack.LOG.warn("AutoUpdate GitHub API returned status {}", response.statusCode());
                }
            } catch (Exception e) {
                splashStatus = "Chuck Pack update check failed";
                ChuckPack.LOG.warn("Failed to check for updates: {}", e.getMessage());
            }
        });
    }

    public static void downloadUpdate() {
        if (downloadUrl.isEmpty()) {
            ChuckPack.LOG.warn("No download URL available. Run check first.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                Path currentJar = getCurrentJarPath();
                // Use the actual file's directory (handles Barebones nrc-26.1.2-fabric subfolder and USB) — not just mods
                Path modsDir = (currentJar != null && currentJar.getParent() != null) ? currentJar.getParent() : FabricLoader.getInstance().getGameDir().resolve("mods");
                if (ChuckPackConfigModifier.get().debugLogging.get()) {
                    ChuckPack.LOG.info("AutoUpdate: currentJar={} modsDir={}", currentJar, modsDir);
                }
                Path tempJar = modsDir.resolve("chuck-pack-update.jar");

                ChuckPack.LOG.info("Downloading update from {} ...", downloadUrl);
                HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(downloadUrl))
                    .header("User-Agent", "ChuckPack-AutoUpdater")
                    .GET()
                    .build();

                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() == 200) {
                    Files.copy(response.body(), tempJar, StandardCopyOption.REPLACE_EXISTING);

                    Path targetJar = latestFileName.isEmpty() ? currentJar : modsDir.resolve(latestFileName);
                    if (targetJar == null) targetJar = tempJar;

                    if (currentJar != null && Files.exists(currentJar)) {
                        Path backup = modsDir.resolve("chuck-pack-backup.jar");
                        try {
                            forceMove(currentJar, backup);
                            ChuckPack.LOG.info("Backed up old jar to {}", backup.getFileName());
                        } catch (Exception e) {
                            ChuckPack.LOG.warn("Backup failed (file in use, will retry on restart): {}", e.getMessage());
                            try { currentJar.toFile().deleteOnExit(); } catch (Throwable ignored) {}
                        }
                        try {
                            forceMove(tempJar, targetJar);
                        } catch (Exception e) {
                            // Fallback: copy and mark old for deletion (Windows file lock)
                            try { Files.copy(tempJar, targetJar, StandardCopyOption.REPLACE_EXISTING); Files.deleteIfExists(tempJar); } catch (Throwable ignored) {}
                            try { currentJar.toFile().deleteOnExit(); } catch (Throwable ignored) {}
                            ChuckPack.LOG.warn("Force move fallback used for {}", targetJar.getFileName());
                        }
                        splashStatus = "Chuck Pack updated & downloaded: " + latestVersion + " (restart)";
                        ChuckPack.LOG.info("Update installed! Restart Minecraft to apply. File: {} Backup: {} (was {} in {})", targetJar.getFileName(), backup.getFileName(), currentJar.getFileName(), modsDir);
                        cleanOldDuplicates(modsDir, targetJar.getFileName().toString());
                    } else if (!targetJar.equals(tempJar)) {
                        try { forceMove(tempJar, targetJar); } catch (Exception e) { try { Files.copy(tempJar, targetJar, StandardCopyOption.REPLACE_EXISTING); } catch (Throwable ignored) {} }
                        splashStatus = "Chuck Pack updated & downloaded: " + latestVersion + " (restart)";
                        ChuckPack.LOG.info("Update downloaded to: {}. Restart Minecraft to apply.", targetJar);
                        cleanOldDuplicates(modsDir, targetJar.getFileName().toString());
                    } else {
                        ChuckPack.LOG.info("Update downloaded to: {}. Manually replace current jar and restart.", tempJar);
                    }
                } else {
                    ChuckPack.LOG.warn("Download failed with status {}", response.statusCode());
                }
            } catch (Exception e) {
                ChuckPack.LOG.warn("Download failed: {}", e.getMessage());
            }
        });
    }

    private static void forceMove(Path source, Path target) throws Exception {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AccessDeniedException e) {
            // File in use (Windows) — try to force via deleteOnExit + copy
            System.gc();
            Thread.sleep(100);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            try { Files.deleteIfExists(source); } catch (Throwable ignored) { source.toFile().deleteOnExit(); }
        }
    }

    private static void cleanOldDuplicates(Path modsDir, String keepFileName) {
        // Handles any file with Chuck pack in name (chuck-pack, Chuck Pack, chuckpack etc.) case-insensitive
        try (java.nio.file.DirectoryStream<Path> stream = Files.newDirectoryStream(modsDir, "*.jar")) {
            for (Path p : stream) {
                String fn = p.getFileName().toString();
                String lower = fn.toLowerCase();
                boolean isChuckPack = lower.contains("chuck") && lower.contains("pack");
                if (!isChuckPack) continue;
                if (fn.equals(keepFileName) || fn.equals("chuck-pack-backup.jar")) continue;
                if (fn.equals("chuck-pack-update.jar") || fn.equalsIgnoreCase("chuck-pack-update.jar")) {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) { p.toFile().deleteOnExit(); }
                    continue;
                }
                // Delete any other Chuck Pack jar that is not the target (old version duplicate, renamed etc.)
                try {
                    Files.deleteIfExists(p);
                    ChuckPack.LOG.info("Deleted old duplicate mod: {}", fn);
                } catch (java.nio.file.AccessDeniedException e) {
                    p.toFile().deleteOnExit();
                    ChuckPack.LOG.info("Marked old duplicate for deletion on restart (in use): {}", fn);
                } catch (Exception e) {
                    ChuckPack.LOG.warn("Failed to delete duplicate {}: {}", fn, e.getMessage());
                }
            }
        } catch (Exception e) {
            ChuckPack.LOG.warn("Failed to clean old duplicates: {}", e.getMessage());
        }
        // Also clean any lingering update file
        try { Files.deleteIfExists(modsDir.resolve("chuck-pack-update.jar")); } catch (Exception ignored) {}
    }

    private static boolean isNewerVersion(String latest, String current) {
        try {
            String[] lParts = latest.split("\\.");
            String[] cParts = current.split("\\.");
            int len = Math.max(lParts.length, cParts.length);
            for (int i = 0; i < len; i++) {
                int l = i < lParts.length ? Integer.parseInt(lParts[i].replaceAll("[^0-9]", "")) : 0;
                int c = i < cParts.length ? Integer.parseInt(cParts[i].replaceAll("[^0-9]", "")) : 0;
                if (l > c) return true;
                if (l < c) return false;
            }
            return !latest.equals(current);
        } catch (Exception e) {
            return !latest.equals(current);
        }
    }

    private static String getVersion() {
        try {
            ModContainer mod = FabricLoader.getInstance().getModContainer("chuckpack").orElse(null);
            if (mod != null) {
                return mod.getMetadata().getVersion().getFriendlyString();
            }
        } catch (Exception ignored) {}
        return "unknown";
    }

    private static Path getCurrentJarPath() {
        try {
            String path = AutoUpdateChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            path = java.net.URLDecoder.decode(path, "UTF-8");
            return Path.of(path);
        } catch (Exception e) {
            return null;
        }
    }
}
