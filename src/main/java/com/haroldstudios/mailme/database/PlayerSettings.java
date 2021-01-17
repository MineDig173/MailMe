package com.haroldstudios.mailme.database;

import com.haroldstudios.mailme.MailMe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PlayerSettings {

    private final UUID uuid;
    private boolean receivingNotifications;
    private final List<Location> mailboxLocations;
    @Nullable private String languageToken;
    private static transient Map<String, Lock> locks = new HashMap<>();

    public PlayerSettings(final UUID uuid) {
        this.uuid = uuid;
        this.receivingNotifications = true;
        this.mailboxLocations = new ArrayList<>();
    }

    /**
     * Retrieves this player's mailbox locations on the server
     * Does NOT include post office locations
     *
     * @return Mailbox locations
     */
    public List<Location> getMailboxLocations() {
        List<Location> locationsList = new ArrayList<>(mailboxLocations);
        locationsList.add(MailMe.getInstance().getCache().getServerSettings().getDefaultMailboxLocation());
        locationsList.removeAll(Collections.singleton(null));
        return locationsList;
    }

    // Only retrieves the mailbox locations of this player excluding the default mailbox
    public List<Location> getOnlyMailboxLocations() {
        return mailboxLocations;
    }

    /**
     * @return If player setting is set to receive notifications
     */
    public boolean isReceivingNotifications() {
        return receivingNotifications;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void removeMailboxLocation(Location location) {
        this.mailboxLocations.remove(location);
        MailMe.getInstance().getCache().removeMailbox(location);
        save();
    }

    public void removeMailboxLocations(List<Location> locations) {
        MailMe.getInstance().getCache().removeAllMailboxes(locations);
        this.mailboxLocations.removeAll(locations);
        save();
    }

    public void addMailboxLocation(Location location) {
        this.mailboxLocations.add(location);
        MailMe.getInstance().getCache().addMailbox(getUuid(), location);
        save();
    }

    public void setReceivingNotifications(boolean receivingNotifications) {
        this.receivingNotifications = receivingNotifications;
        save();
    }

    public String getLanguageToken() {
        return languageToken;
    }

    public void setLanguageToken(String languageToken) {
        this.languageToken = languageToken;
        save();
    }

    /**
     * Saves player settings to file
     */
    public void save() {
        final Lock lock;
        if (locks == null) locks = new HashMap<>();
        // Create lock for each file if there isn't already one.
        if (locks.containsKey(uuid.toString())) {
            lock = locks.get(uuid.toString());
        } else {
            ReadWriteLock rwl = new ReentrantReadWriteLock();
            lock = rwl.writeLock();
            locks.put(uuid.toString(), lock);
        }

        Bukkit.getScheduler().runTaskAsynchronously(MailMe.getInstance(), () -> {
            boolean success;
            try {
                while(!lock.tryLock(10, TimeUnit.SECONDS)) {}
                success = lock.tryLock();
            } catch (InterruptedException e) {
                MailMe.debug(e);
                return;
            }
            if (!success) {
                MailMe.debug(PlayerSettings.class, "File was still locked when attempting to save! " + uuid);
            }
            lock.lock();
            MailMe.getInstance().getCache().getFileUtil().save(this, new File(MailMe.getInstance().getDataFolder() + "/playersettings/" + uuid.toString() + ".json"));
            lock.unlock();
        });

    }


}
