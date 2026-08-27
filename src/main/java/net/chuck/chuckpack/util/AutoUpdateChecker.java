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
    private static boolean updateAvailable = false;

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

                    if (!currentClean.equals(latestClean)) {
                        updateAvailable = true;
                        ChuckPack.LOG.info("Update available: {} -> {}", currentClean, latestClean);

                        if (json.has("assets")) {
                            for (var asset : json.getAsJsonArray("assets")) {
                                JsonObject assetObj = asset.getAsJsonObject();
                                String name = assetObj.get("name").getAsString();
                                if (name.endsWith(".jar") && name.contains("chuck-pack")) {
                                    downloadUrl = assetObj.get("browser_download_url").getAsString();
                                    ChuckPack.LOG.info("Download: {}", downloadUrl);
                                    break;
                                }
                            }
                        }

                        if (autoDownload && !downloadUrl.isEmpty()) {
                            downloadUpdate();
                        } else {
                            ChuckPack.LOG.info("Open Config > Chuck Pack or check GitHub Releases to update. URL: {}", downloadUrl);
                        }
                    } else {
                        updateAvailable = false;
                        ChuckPack.LOG.info("Chuck Pack is up to date: {}", currentClean);
                    }
                } else {
                    ChuckPack.LOG.warn("AutoUpdate GitHub API returned status {}", response.statusCode());
                }
            } catch (Exception e) {
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
                Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
                Path currentJar = getCurrentJarPath();
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

                    if (currentJar != null && Files.exists(currentJar)) {
                        Path backup = modsDir.resolve("chuck-pack-backup.jar");
                        Files.move(currentJar, backup, StandardCopyOption.REPLACE_EXISTING);
                        Files.move(tempJar, currentJar, StandardCopyOption.REPLACE_EXISTING);
                        ChuckPack.LOG.info("Update installed! Restart Minecraft to apply. Backup: {}", backup.getFileName());
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
