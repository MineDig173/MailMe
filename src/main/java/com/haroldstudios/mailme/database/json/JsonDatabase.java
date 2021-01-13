package com.haroldstudios.mailme.database.json;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.mail.Mail;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class JsonDatabase implements PlayerMailDAO {

    private final MailMe plugin;

    public JsonDatabase(final MailMe plugin) {
        this.plugin = plugin;
        new File(plugin.getDataFolder(), "playerdata_json").mkdir();
    }

    @Override
    public CompletableFuture<Mail[]> getAllMail(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerDataFile data = PlayerDataFile.getPlayerDataFile(uuid, plugin);
            Collection<Mail> mail = data.getAllMail();
            mail = mail.stream().filter(m -> !isExpired(uuid, m, m.getDateReceived())).collect(Collectors.toList());
            List<Mail> dirtyReverse = Arrays.asList(mail.toArray(new Mail[0]));
            Collections.reverse(dirtyReverse);
            return dirtyReverse.toArray(new Mail[0]);
        }).exceptionally(e -> {
            MailMe.debug(e);
            return new Mail[0];
        });
    }

    @Override
    public CompletableFuture<Mail[]> getUnreadMail(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerDataFile data = PlayerDataFile.getPlayerDataFile(uuid, plugin);
            Collection<Mail> mail = data.getAllMail();
            mail = mail.stream().filter(m -> !m.isRead()).filter(m -> !isExpired(uuid, m, m.getDateReceived())).collect(Collectors.toList());
            List<Mail> dirtyReverse = Arrays.asList(mail.toArray(new Mail[0]));
            Collections.reverse(dirtyReverse);
            return dirtyReverse.toArray(new Mail[0]);
        }).exceptionally(e -> {
            MailMe.debug(e);
            return new Mail[0];
        });
    }

    @Override
    public CompletableFuture<Boolean> hasUnreadMail(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerDataFile data = PlayerDataFile.getPlayerDataFile(uuid, plugin);
            Collection<Mail> mail = data.getAllMail();
            mail = mail.stream().filter(m -> !m.isRead()).collect(Collectors.toList());
            return mail.size() > 0;
        }).exceptionally(e -> {
            MailMe.debug(e);
            return false;
        });
    }

    @Override
    public CompletableFuture<Boolean> setUnread(UUID uuid, Mail mail) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerDataFile data = PlayerDataFile.getPlayerDataFile(uuid, plugin);
            mail.setRead(true);
            return data.updateMail(mail.getColId(), mail);
        }).exceptionally(e -> {
            MailMe.debug(e);
            return false;
        });
    }

    @Override
    public CompletableFuture<Boolean> savePreset(Mail mail) {
        return CompletableFuture.supplyAsync(() -> {
            JsonPresetFile presets = JsonPresetFile.getJsonPresetFile(plugin);
            presets.addPreset(mail);
            return true;
        }).exceptionally(e -> {
            MailMe.debug(e);
            return false;
        });
    }

    @Override
    public CompletableFuture<Mail> getPresetMail(String presetName) {
        return CompletableFuture.supplyAsync(() -> {
            JsonPresetFile presets = JsonPresetFile.getJsonPresetFile(plugin);
            return presets.getPreset(presetName);
        }).exceptionally(e -> {
            MailMe.debug(e);
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> deletePresetMail(String presetName) {
        return CompletableFuture.supplyAsync(() -> {
            JsonPresetFile presets = JsonPresetFile.getJsonPresetFile(plugin);
            return presets.deletePreset(presetName);
        }).exceptionally(e -> {
            MailMe.debug(e);
            return false;
        });
    }

    @Override
    public CompletableFuture<Set<String>> getPresetMailIdentifiers() {
        return CompletableFuture.supplyAsync(() -> {
            JsonPresetFile presets = JsonPresetFile.getJsonPresetFile(plugin);
            return presets.getIdentifiers();
        }).exceptionally(e -> {
            MailMe.debug(e);
            return new HashSet<>();
        });
    }

    @Override
    public void deletePlayerMail(UUID uuid, Mail mail) {
        CompletableFuture.runAsync(() -> {
            PlayerDataFile data = PlayerDataFile.getPlayerDataFile(uuid, plugin);
            data.deleteMail(mail);
        }).exceptionally(e -> {
            MailMe.debug(e);
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> saveMailObj(Mail mail) {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> savePlayerMail(UUID uuid, Mail mail) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerDataFile data = PlayerDataFile.getPlayerDataFile(uuid, plugin);
            data.insertMail(mail);
            return true;
        }).exceptionally(e -> {
            MailMe.debug(e);
            return false;
        });
    }

    private boolean isExpired(UUID uuid, Mail mail, long timeMillis) {

        boolean expired = timeMillis + mail.getExpiryTimeMilliSeconds() < System.currentTimeMillis();
        if (expired) {
            CompletableFuture.runAsync(() -> {
                deletePlayerMail(uuid, mail);
            });
        }

        return expired;
    }
}
