package net.chuck.chuckpack.modules.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
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

public class AutoUpdate extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> checkOnStartup = sgGeneral.add(new BoolSetting.Builder()
        .name("Check on Startup")
        .description("Check for updates when the mod loads.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoDownload = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Download")
        .description("Automatically download and install updates when found.")
        .defaultValue(false)
        .build()
    );

    private GuiTheme cachedTheme;
    private boolean updateAvailable = false;
    private String latestVersion = "";
    private String downloadUrl = "";
    private String currentVersion = "";

    public AutoUpdate() {
        super(Categories.Misc, "Auto Update", "Check for and install Chuck Pack updates from GitHub.");
    }

    @Override
    public void onActivate() {
        currentVersion = getVersion();
        if (checkOnStartup.get()) {
            checkForUpdates();
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        this.cachedTheme = theme;
        var table = theme.table();

        WButton checkBtn = table.add(theme.button("Check for Updates")).expandX().minWidth(100).widget();
        checkBtn.action = this::checkForUpdates;

        WButton downloadBtn = table.add(theme.button("Download & Install")).expandX().minWidth(100).widget();
        downloadBtn.action = () -> {
            if (updateAvailable) {
                downloadUpdate();
            } else {
                ChatUtils.info("No update available.");
            }
        };

        WButton openFolderBtn = table.add(theme.button("Open Mods Folder")).expandX().minWidth(100).widget();
        openFolderBtn.action = this::openModsFolder;

        return table;
    }

    private void checkForUpdates() {
        CompletableFuture.runAsync(() -> {
            try {
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
                        info("Update available: %s -> %s", currentClean, latestClean);

                        // Find jar asset
                        if (json.has("assets")) {
                            for (var asset : json.getAsJsonArray("assets")) {
                                JsonObject assetObj = asset.getAsJsonObject();
                                String name = assetObj.get("name").getAsString();
                                if (name.endsWith(".jar") && name.contains("chuck-pack")) {
                                    downloadUrl = assetObj.get("browser_download_url").getAsString();
                                    info("Download: %s", downloadUrl);
                                    break;
                                }
                            }
                        }

                        if (autoDownload.get() && !downloadUrl.isEmpty()) {
                            downloadUpdate();
                        }
                    } else {
                        updateAvailable = false;
                        info("You are on the latest version: %s", currentClean);
                    }
                } else {
                    error("GitHub API returned status %d", response.statusCode());
                }
            } catch (Exception e) {
                error("Failed to check for updates: %s", e.getMessage());
            }
        });
    }

    private void downloadUpdate() {
        if (downloadUrl.isEmpty()) {
            error("No download URL available.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
                Path currentJar = getCurrentJarPath();
                Path tempJar = modsDir.resolve("chuck-pack-update.jar");

                info("Downloading update...");
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

                    // Replace current jar
                    if (currentJar != null) {
                        Path backup = modsDir.resolve("chuck-pack-backup.jar");
                        Files.move(currentJar, backup, StandardCopyOption.REPLACE_EXISTING);
                        Files.move(tempJar, currentJar, StandardCopyOption.REPLACE_EXISTING);
                        info("Update installed! Restart Minecraft to apply.");
                        info("Backup saved as: %s", backup.getFileName());
                    } else {
                        info("Update downloaded to: %s", tempJar);
                        info("Manually replace the current jar and restart.");
                    }
                } else {
                    error("Download failed with status %d", response.statusCode());
                }
            } catch (Exception e) {
                error("Download failed: %s", e.getMessage());
            }
        });
    }

    private void openModsFolder() {
        try {
            Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
            java.awt.Desktop.getDesktop().open(modsDir.toFile());
        } catch (Exception e) {
            error("Failed to open mods folder: %s", e.getMessage());
        }
    }

    private String getVersion() {
        try {
            ModContainer mod = FabricLoader.getInstance().getModContainer("chuckpack").orElse(null);
            if (mod != null) {
                return mod.getMetadata().getVersion().getFriendlyString();
            }
        } catch (Exception ignored) {}
        return "unknown";
    }

    private Path getCurrentJarPath() {
        try {
            // Try to find the running jar
            String path = AutoUpdate.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            // Decode URL-encoded characters
            path = java.net.URLDecoder.decode(path, "UTF-8");
            return Path.of(path);
        } catch (Exception e) {
            return null;
        }
    }
}
