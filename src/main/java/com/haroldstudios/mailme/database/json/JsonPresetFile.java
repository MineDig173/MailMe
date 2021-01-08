package com.haroldstudios.mailme.database.json;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonPresetFile {

    private final Map<String, Mail> presets = new HashMap<>();

    public static JsonPresetFile getJsonPresetFile(MailMe plugin) {
        return plugin.getCache().getFileUtil().getFile(JsonPresetFile.class).exists() ? plugin.getCache().getFileUtil().load(JsonPresetFile.class) : new JsonPresetFile();
    }

    public Set<String> getIdentifiers() {
        return presets.keySet();
    }

    public boolean addPreset(Mail mail) {
        if (presets.containsKey(mail.getIdentifier())) return false;
        presets.put(mail.getIdentifier(), mail);
        save();
        return true;
    }

    public boolean deletePreset(String identifier) {
        boolean result =  presets.remove(identifier) != null;
        save();
        return result;
    }

    public Mail getPreset(String identifier) {
        if (!presets.containsKey(identifier)) return null;
        return presets.get(identifier);
    }

    public void save() {
        Bukkit.getScheduler().runTaskAsynchronously(MailMe.getInstance(), () -> {
            MailMe.getInstance().getCache().getFileUtil().save(this);
        });
    }
}
