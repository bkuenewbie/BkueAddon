package com.bkueAddon.updater;

import com.bkueAddon.BkueAddon;
import daybreak.abilitywar.AbilityWar;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BkueAddonUpdater implements Listener {
    private static final String OWNER = "bkuenewbie";
    private static final String REPOSITORY = "BkueAddon";

    private static final String API_URL =
            "https://api.github.com/repos/"
                    + OWNER
                    + "/"
                    + REPOSITORY
                    + "/releases";

    private final BkueAddon addon;

    private String currentVersion;
    private String latestVersion;
    private String latestDownloadUrl;

    private volatile boolean checking;
    private volatile boolean downloading;

    public BkueAddonUpdater(BkueAddon addon) {
        this.addon = addon;
        this.currentVersion = readCurrentVersion();

        Bukkit.getPluginManager().registerEvents(
                this,
                AbilityWar.getPlugin()
        );
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public boolean isUpdateAvailable() {
        return latestVersion != null
                && compareVersions(latestVersion, currentVersion) > 0;
    }

    public void check(CommandSender sender) {

        if (checking) {
            sender.sendMessage(
                    "§b[BkueAddon] §f이미 업데이트를 확인하고 있습니다."
            );
            return;
        }

        checking = true;

        sender.sendMessage(
                "§b[BkueAddon] §fGitHub에서 최신 버전을 확인하는 중..."
        );

        CompletableFuture.runAsync(() -> {

            try {

                Release release = getLatestRelease();

                if (release == null) {
                    send(sender,
                            "§c[BkueAddon] §fGitHub에서 릴리즈를 찾을 수 없습니다."
                    );
                    return;
                }

                latestVersion = cleanVersion(release.version);
                latestDownloadUrl = release.downloadUrl;

                if (isUpdateAvailable()) {
                    sendUpdateAvailable(sender);
                } else {
                    send(sender,
                            "§b[BkueAddon] §a현재 최신 버전입니다. §7(v"
                                    + currentVersion
                                    + "§7)"
                    );
                }

            } catch (Exception e) {

                send(sender,
                        "§c[BkueAddon] §f업데이트 확인 중 오류가 발생했습니다."
                );

                e.printStackTrace();

            } finally {
                checking = false;
            }

        });
    }

    public void update(CommandSender sender) {

        if (downloading) {
            sender.sendMessage(
                    "§b[BkueAddon] §f이미 업데이트를 다운로드하고 있습니다."
            );
            return;
        }

        downloading = true;

        CompletableFuture.runAsync(() -> {

            try {

                Release release = getLatestRelease();

                if (release == null) {
                    send(sender,
                            "§c[BkueAddon] §f다운로드할 릴리즈를 찾을 수 없습니다."
                    );
                    return;
                }

                latestVersion = cleanVersion(release.version);
                latestDownloadUrl = release.downloadUrl;

                if (compareVersions(
                        latestVersion,
                        currentVersion
                ) <= 0) {

                    send(sender,
                            "§b[BkueAddon] §a이미 최신 버전입니다. §7(v"
                                    + currentVersion
                                    + "§7)"
                    );

                    return;
                }

                downloadUpdate(sender, release);

            } catch (Exception e) {

                send(sender,
                        "§c[BkueAddon] §f업데이트 중 오류가 발생했습니다."
                );

                e.printStackTrace();

            } finally {
                downloading = false;
            }

        });
    }

    public void updateToVersion(
            CommandSender sender,
            String requestedVersion
    ) {

        if (downloading) {
            sender.sendMessage(
                    "§b[BkueAddon] §f이미 업데이트를 다운로드하고 있습니다."
            );
            return;
        }

        downloading = true;

        CompletableFuture.runAsync(() -> {

            try {

                String wanted =
                        cleanVersion(requestedVersion);

                Release release =
                        findRelease(wanted);

                if (release == null) {

                    send(sender,
                            "§c[BkueAddon] §f버전 §e"
                                    + wanted
                                    + "§f을 찾을 수 없습니다."
                    );

                    return;
                }

                latestVersion =
                        cleanVersion(release.version);

                latestDownloadUrl =
                        release.downloadUrl;

                if (compareVersions(
                        latestVersion,
                        currentVersion
                ) == 0) {

                    send(sender,
                            "§b[BkueAddon] §f이미 §e"
                                    + currentVersion
                                    + "§f 버전을 사용하고 있습니다."
                    );

                    return;
                }

                downloadUpdate(
                        sender,
                        release
                );

            } catch (Exception e) {

                send(sender,
                        "§c[BkueAddon] §f업데이트 중 오류가 발생했습니다."
                );

                e.printStackTrace();

            } finally {
                downloading = false;
            }

        });
    }

    private void downloadUpdate(
            CommandSender sender,
            Release release
    ) throws IOException {

        String version =
                cleanVersion(release.version);

        send(sender,
                "§b[BkueAddon] §fBkueAddon §e"
                        + version
                        + "§f 다운로드를 시작합니다..."
        );

        File addonFolder =
                getAddonFolder();

        if (!addonFolder.exists()
                && !addonFolder.mkdirs()) {

            throw new IOException(
                    "AbilityWar Addon directory could not be created."
            );
        }

        File temporaryJar =
                new File(
                        addonFolder,
                        "BkueAddon-" + version + ".jar.download"
                );

        File targetJar =
                new File(
                        addonFolder,
                        "BkueAddon-" + version + ".jar"
                );

        try {

            downloadFile(
                    release.downloadUrl,
                    temporaryJar,
                    sender
            );

            validateJar(temporaryJar);

            Files.move(
                    temporaryJar.toPath(),
                    targetJar.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

        } finally {

            if (temporaryJar.exists()) {
                Files.deleteIfExists(
                        temporaryJar.toPath()
                );
            }
        }

        send(sender,
                "§b[BkueAddon] §a업데이트 다운로드가 완료되었습니다!"
        );

        send(sender,
                "§7현재 버전: §e"
                        + currentVersion
                        + " §7→ §a"
                        + version
        );

        send(sender,
                "§e새로운 버전은 서버를 재시작하면 적용됩니다."
        );

        send(sender,
                "§7기존 버전은 재시작 전까지 삭제하지 않습니다."
        );
    }

    private File getAddonFolder() {

        File abilityWarFolder =
                AbilityWar.getPlugin().getDataFolder();

        return new File(
                abilityWarFolder,
                "Addon"
        );
    }

    private void downloadFile(
            String downloadUrl,
            File destination,
            CommandSender sender
    ) throws IOException {

        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(downloadUrl)
                                .openConnection();

        connection.setRequestMethod("GET");

        connection.setRequestProperty(
                "User-Agent",
                "BkueAddon-Updater"
        );

        connection.setRequestProperty(
                "Accept",
                "application/octet-stream"
        );

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.setInstanceFollowRedirects(true);

        int response =
                connection.getResponseCode();

        if (response != HttpURLConnection.HTTP_OK) {

            throw new IOException(
                    "GitHub returned HTTP "
                            + response
            );
        }

        long total =
                connection.getContentLengthLong();

        try (
                InputStream input =
                        connection.getInputStream();

                FileOutputStream output =
                        new FileOutputStream(destination)
        ) {

            byte[] buffer =
                    new byte[8192];

            long downloaded = 0;

            int lastPercent = -10;

            int read;

            while ((read =
                    input.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        read
                );

                downloaded += read;

                if (total > 0) {

                    int percent =
                            (int)
                                    ((downloaded * 100)
                                            / total);

                    if (percent
                            >= lastPercent + 10) {

                        lastPercent =
                                percent;

                        send(
                                sender,
                                "§b[BkueAddon] §f다운로드 §e"
                                        + percent
                                        + "% §f완료"
                        );
                    }
                }
            }

        } finally {
            connection.disconnect();
        }
    }

    private void validateJar(File jar)
            throws IOException {

        if (!jar.exists()
                || jar.length() < 1024) {

            throw new IOException(
                    "Downloaded JAR is invalid."
            );
        }

        try (
                java.util.jar.JarFile jarFile =
                        new java.util.jar.JarFile(jar)
        ) {

            if (jarFile.getJarEntry(
                    "addon.yml"
            ) == null) {

                throw new IOException(
                        "Downloaded JAR does not contain addon.yml."
                );
            }
        }
    }

    private Release getLatestRelease()
            throws Exception {

        List<Release> releases =
                getReleases();

        Release latest = null;

        for (Release release : releases) {

            if (release.prerelease
                    || release.draft) {

                continue;
            }

            if (latest == null
                    || compareVersions(
                    release.version,
                    latest.version
            ) > 0) {

                latest = release;
            }
        }

        return latest;
    }

    private Release findRelease(
            String version
    ) throws Exception {

        List<Release> releases =
                getReleases();

        for (Release release : releases) {

            if (cleanVersion(
                    release.version
            ).equalsIgnoreCase(
                    cleanVersion(version)
            )) {

                return release;
            }
        }

        return null;
    }

    private List<Release> getReleases()
            throws Exception {

        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(API_URL)
                                .openConnection();

        connection.setRequestMethod("GET");

        connection.setRequestProperty(
                "Accept",
                "application/vnd.github+json"
        );

        connection.setRequestProperty(
                "User-Agent",
                "BkueAddon-Updater"
        );

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        int response =
                connection.getResponseCode();

        if (response != HttpURLConnection.HTTP_OK) {

            throw new IOException(
                    "GitHub returned HTTP "
                            + response
            );
        }

        String json;

        try (InputStream input =
                     connection.getInputStream()) {

            json =
                    new String(
                            input.readAllBytes(),
                            StandardCharsets.UTF_8
                    );

        } finally {
            connection.disconnect();
        }

        return parseReleases(json);
    }

    private List<Release> parseReleases(
            String json
    ) {

        List<Release> releases =
                new ArrayList<>();

        Pattern releasePattern =
                Pattern.compile(
                        "\"tag_name\"\\s*:\\s*\"([^\"]+)\""
                                + "(?s:.*?)"
                                + "\"assets\"\\s*:\\s*\\[(.*?)\\]"
                );

        Matcher matcher =
                releasePattern.matcher(json);

        while (matcher.find()) {

            String version =
                    matcher.group(1);

            String assets =
                    matcher.group(2);

            String downloadUrl =
                    findJarUrl(assets);

            if (downloadUrl == null) {
                continue;
            }

            int releaseStart =
                    matcher.start();

            int releaseEnd =
                    matcher.end();

            String releaseObject =
                    json.substring(
                            releaseStart,
                            releaseEnd
                    );

            boolean prerelease =
                    releaseObject.contains(
                            "\"prerelease\":true"
                    );

            boolean draft =
                    releaseObject.contains(
                            "\"draft\":true"
                    );

            releases.add(
                    new Release(
                            version,
                            downloadUrl,
                            prerelease,
                            draft
                    )
            );
        }

        return releases;
    }

    private String findJarUrl(
            String assets
    ) {

        Pattern pattern =
                Pattern.compile(
                        "\"browser_download_url\"\\s*:\\s*\"([^\"]+\\.jar)\""
                );

        Matcher matcher =
                pattern.matcher(assets);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String readCurrentVersion() {

        String version =
                addon.getDescription()
                        .getVersion();

        if (version == null
                || version.isBlank()) {

            return "0.0.0";
        }

        return cleanVersion(version);
    }

    private String cleanVersion(
            String version
    ) {

        if (version == null) {
            return "0.0.0";
        }

        version =
                version.trim();

        while (
                version.startsWith("v")
                        || version.startsWith("V")
        ) {

            version =
                    version.substring(1);
        }

        int dash =
                version.indexOf("-");

        if (dash >= 0) {

            version =
                    version.substring(
                            0,
                            dash
                    );
        }

        return version;
    }

    private int compareVersions(
            String first,
            String second
    ) {

        String[] a =
                cleanVersion(first)
                        .split("\\.");

        String[] b =
                cleanVersion(second)
                        .split("\\.");

        int length =
                Math.max(
                        a.length,
                        b.length
                );

        for (int i = 0;
             i < length;
             i++) {

            int ai =
                    i < a.length
                            ? parseNumber(a[i])
                            : 0;

            int bi =
                    i < b.length
                            ? parseNumber(b[i])
                            : 0;

            if (ai != bi) {

                return Integer.compare(
                        ai,
                        bi
                );
            }
        }

        return 0;
    }

    private int parseNumber(
            String value
    ) {

        try {

            return Integer.parseInt(
                    value.replaceAll(
                            "[^0-9]",
                            ""
                    )
            );

        } catch (Exception e) {

            return 0;
        }
    }

    private void sendUpdateAvailable(
            CommandSender sender
    ) {

        TextComponent message =
                new TextComponent(
                        "§b[BkueAddon] §f새로운 버전이 있습니다!\n"
                );

        message.addExtra(
                new TextComponent(
                        "§7현재 버전: §e"
                                + currentVersion
                                + " §7→ 최신 버전: §a"
                                + latestVersion
                                + "\n"
                )
        );

        TextComponent update =
                new TextComponent(
                        "§a§l[ 업데이트 ]"
                );

        update.setClickEvent(
                new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/aw bkueaddon update"
                )
        );

        update.setHoverEvent(
                new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(
                                "§e클릭하여 업데이트합니다."
                        ).create()
                )
        );

        TextComponent check =
                new TextComponent(
                        " §7§l[ 확인 ]"
                );

        check.setClickEvent(
                new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/aw bkueaddon check"
                )
        );

        check.setHoverEvent(
                new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(
                                "§e최신 버전을 다시 확인합니다."
                        ).create()
                )
        );

        message.addExtra(update);
        message.addExtra(check);

        if (sender instanceof Player player) {

            player.spigot()
                    .sendMessage(message);

        } else {

            sender.sendMessage(
                    ChatColor.AQUA
                            + "[BkueAddon] "
                            + ChatColor.WHITE
                            + "새로운 버전: "
                            + latestVersion
            );
        }
    }

    private void send(
            CommandSender sender,
            String message
    ) {

        Bukkit.getScheduler().runTask(
                AbilityWar.getPlugin(),
                () -> sender.sendMessage(message)
        );
    }

    @EventHandler
    public void onCommand(
            PlayerCommandPreprocessEvent event
    ) {

        String message =
                event.getMessage()
                        .trim();

        if (!message
                .toLowerCase()
                .startsWith("/aw ")) {

            return;
        }

        String[] args =
                message.substring(4)
                        .trim()
                        .split("\\s+");

        if (args.length == 0) {
            return;
        }

        if (!args[0]
                .equalsIgnoreCase(
                        "bkueaddon"
                )) {

            return;
        }

        event.setCancelled(true);

        Player player =
                event.getPlayer();

        if (!player.isOp()) {

            player.sendMessage(
                    "§c[BkueAddon] §f이 명령어는 OP만 사용할 수 있습니다."
            );

            return;
        }

        if (args.length == 1) {

            check(player);
            return;
        }

        switch (
                args[1].toLowerCase()
        ) {

            case "check":
                check(player);
                break;

            case "update":
                update(player);
                break;

            default:
                updateToVersion(
                        player,
                        args[1]
                );
                break;
        }
    }

    private static class Release {

        private final String version;
        private final String downloadUrl;
        private final boolean prerelease;
        private final boolean draft;

        private Release(
                String version,
                String downloadUrl,
                boolean prerelease,
                boolean draft
        ) {

            this.version = version;
            this.downloadUrl = downloadUrl;
            this.prerelease = prerelease;
            this.draft = draft;
        }
    }
}
