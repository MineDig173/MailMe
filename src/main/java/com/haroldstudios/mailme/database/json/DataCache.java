package com.haroldstudios.mailme.database.json;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerSettings;
import com.haroldstudios.mailme.database.json.serializer.FileUtil;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataCache {

    private final FileUtil fileUtil;

    private final Map<UUID, PlayerSettings> playerSettingsMap = new HashMap<>();

    public DataCache() {
        this.fileUtil = new FileUtil();
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

    public PlayerSettings getPlayerSettings(Player player) {
        return getPlayerSettings(player.getUniqueId());
    }

    public FileUtil getFileUtil() {
        return fileUtil;
    }
}
