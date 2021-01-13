package com.haroldstudios.mailme.database.transition;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.database.json.JsonPresetFile;
import com.haroldstudios.mailme.database.json.PlayerDataFile;
import com.haroldstudios.mailme.database.sql.MySQLDatabase;
import com.haroldstudios.mailme.mail.Mail;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.UUID;

//TODO Future release
public class Json2MySQL implements Transitionable {

    private final MailMe plugin;
    private final PlayerMailDAO mySQLDatabase;

    public Json2MySQL(final MailMe plugin) {
        this.plugin = plugin;

        this.mySQLDatabase = plugin.getPlayerMailDAO();
        if (!(mySQLDatabase instanceof MySQLDatabase)) {
            plugin.getLogger().warning("Invalid database transition! Trying to convert from json to " + mySQLDatabase.getClass() + " using the " + Json2MySQL.class + " transitionable!");
        }
    }

    @Override
    public void transitionMail() {

        File jsonFolder = new File(plugin.getDataFolder(), "playerdata_json");
        if (!jsonFolder.exists()){
            plugin.getLogger().warning("Could not find playerdata_json folder to convert from.");
            return;
        }

        // Playerdata files
        for (File data : jsonFolder.listFiles()) {
            PlayerDataFile dataFile = plugin.getCache().getFileUtil().load(PlayerDataFile.class, data);
            String uuidString = FilenameUtils.getBaseName(data.getName());
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException ex){
                MailMe.debug(Json2MySQL.class, "Could not convert to uuid: " + uuidString);
                continue;
            }

            if (dataFile == null) {
                MailMe.debug(Json2MySQL.class, "Couldn't convert datafile for: " + data);
                continue;
            }

            for (Mail mail : dataFile.getAllMail()) {
                mySQLDatabase.saveMailObj(mail);
                mySQLDatabase.savePlayerMail(uuid, mail);
            }
        }

        // Convert preset file
        JsonPresetFile presetFile = plugin.getCache().getFileUtil().getFile(JsonPresetFile.class).exists() ? plugin.getCache().getFileUtil().load(JsonPresetFile.class) : null;
        if (presetFile == null) {
            MailMe.debug(Json2MySQL.class, "Couldn't load the json preset file");
            return;
        }

        for (String id : presetFile.getIdentifiers()) {
            Mail preset = presetFile.getPreset(id);
            if (preset == null) {
                MailMe.debug(Json2MySQL.class, "Couldn't retrieve preset with id: " + id + "! But it was already loaded!");
                continue;
            }
            mySQLDatabase.savePreset(preset);
        }

    }
}
