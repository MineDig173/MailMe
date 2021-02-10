package com.haroldstudios.mailme.database.json;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class PlayerDataFile {

    private final UUID uuid;
    private final LinkedHashMap<Integer, Mail> mail = new LinkedHashMap<>();
    private int key = 0;

    public PlayerDataFile(final UUID uuid) {
        this.uuid = uuid;
    }

    public static PlayerDataFile getPlayerDataFile(final UUID uuid, final MailMe plugin) {
        File file = new File(MailMe.getInstance().getDataFolder() + "/playerdata_json/" + uuid.toString() + ".json");

        if (file.exists()) {
            return plugin.getCache().getFileUtil().load(PlayerDataFile.class, file);
        }
        return new PlayerDataFile(uuid);
    }

    public void deleteMail(Mail[] mail) {
        for (Mail mail1 : mail) {
            this.mail.remove(mail1.getColId());
        }
        save();
    }

    public void deleteMail(Mail mail) {
        this.mail.remove(mail.getColId());
        save();
    }

    public Collection<Mail> getAllMail() {
        return mail.values();
    }

    public boolean updateMail(int colId, Mail mail) {
        boolean success =  this.mail.replace(colId, mail) != null;
        save();
        return success;
    }

    public Mail getMail(int colId) {
        return this.mail.get(colId);
    }

    public synchronized void insertMail(Mail mail) {
        insertMailNoSave(mail);
        save();
    }

    public synchronized void insertMail(List<Mail> mails) {
        for (Mail mail : mails) {
            insertMailNoSave(mail);
        }
        save();
    }

    private synchronized void insertMailNoSave(Mail mail) {
        key++;
        // May be linked when sending to multiple players. Should overwrite and ignore since we don't store it in a cache currently.
        mail.setColId(key);
        if (!mail.isLegacy()) {
            mail.setRead(false);
        }
        mail.setDateReceived(System.currentTimeMillis());

        this.mail.put(key, mail);
    }


    public synchronized void save() {
        Bukkit.getScheduler().runTaskAsynchronously(MailMe.getInstance(), () -> {
            MailMe.getInstance().getCache().getFileUtil().save(this, new File(MailMe.getInstance().getDataFolder() + "/playerdata_json/" + uuid.toString() + ".json"));
        });
    }
}