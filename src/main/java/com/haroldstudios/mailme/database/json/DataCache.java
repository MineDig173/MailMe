package com.haroldstudios.mailme.database.json;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerSettings;
import com.haroldstudios.mailme.database.ServerSettings;
import com.haroldstudios.mailme.database.json.serializer.FileUtil;
import com.haroldstudios.mailme.postoffice.PostOfficeStore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataCache {

    private final FileUtil fileUtil;

    private final PostOfficeStore postOfficeStore;
    private final Map<UUID, PlayerSettings> playerSettingsMap = new HashMap<>();
    private final ServerSettings serverSettings;
    private final Map<Location, UUID> mailboxLocations = new HashMap<>();

    public DataCache() {
        this.fileUtil = new FileUtil();
        File folder = new File(MailMe.getInstance().getDataFolder(), "playersettings");
        folder.mkdir();
        this.postOfficeStore = this.getFileUtil().getFile(PostOfficeStore.class).exists() ? this.getFileUtil().load(PostOfficeStore.class) : new PostOfficeStore();
        this.serverSettings = this.getFileUtil().getFile(ServerSettings.class).exists() ? this.getFileUtil().load(ServerSettings.class) : new ServerSettings();
        // Inserts all mailboxes on running server into cache.
        for (File file : folder.listFiles()) {
            PlayerSettings settings = fileUtil.load(PlayerSettings.class, file);
            List<Location> personalMailboxLocations = settings.getOnlyMailboxLocations();
            for (Location loc : personalMailboxLocations) {
                mailboxLocations.put(loc, settings.getUuid());
            }
        }
    }

    public boolean isMailboxAtLocation(Location location) {
        return mailboxLocations.containsKey(location) || location.equals(serverSettings.getDefaultMailboxLocation());
    }

    public void addMailbox(UUID uuid, Location location) {
        mailboxLocations.put(location, uuid);
    }

    public void removeMailbox(Location valueToRemove) {
        Location removalKey = null;

        for (Map.Entry<Location, UUID> entry : mailboxLocations.entrySet()) {
            if (valueToRemove.equals(entry.getValue())) {
                removalKey = entry.getKey();
                break;
            }
        }

        if (removalKey != null) {
            mailboxLocations.remove(removalKey);
        }
    }

    @Nullable
    public UUID getWhoOwnsMailboxAtLocation(Location location) {
        return mailboxLocations.get(location);
    }

    /**
     * Retrieves player settings from file and saves to cache.
     * If player settings does not exist in file, creates file and saves to cache
     *
     * @param uuid UUID of player
     */
    public void addPlayerSettingsToCache(UUID uuid) {
        final File jsonData = new File(MailMe.getInstance().getDataFolder() + "/playersettings/" + uuid.toString() + ".json");
        try {
            jsonData.createNewFile();
        } catch (IOException io) {
            io.printStackTrace();
        }
        PlayerSettings playerSettings = fileUtil.load(PlayerSettings.class, jsonData);

        // If PlayerData exists in file
        if (playerSettings != null) {
            playerSettingsMap.put(uuid, playerSettings);
            return;
        }

        playerSettings = new PlayerSettings(uuid);
        playerSettings.save();
        this.playerSettingsMap.put(uuid, playerSettings);
    }

    public void addPlayerSettingsToCache(Player player) {
        addPlayerSettingsToCache(player.getUniqueId());
    }

    /**
     * Retrieves player setting file for player
     *
     * @param uuid UUID of player
     * @return PlayerSettings file
     */
    public PlayerSettings getPlayerSettings(UUID uuid) {
        if (!playerSettingsMap.containsKey(uuid)) addPlayerSettingsToCache(uuid);
        return playerSettingsMap.get(uuid);
    }

    public PostOfficeStore getPostOfficeStore() {
        return postOfficeStore;
    }

    public PlayerSettings getPlayerSettings(Player player) {
        return getPlayerSettings(player.getUniqueId());
    }

    public FileUtil getFileUtil() {
        return fileUtil;
    }

    public ServerSettings getServerSettings() {
        return serverSettings;
    }
}
