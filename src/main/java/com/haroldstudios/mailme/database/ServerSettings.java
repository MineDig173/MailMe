package com.haroldstudios.mailme.database;

import com.haroldstudios.mailme.MailMe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ServerSettings {

    @Nullable private Location defaultMailboxLocation;
    private String activeDatabase = "json";

    @Nullable public Location getDefaultMailboxLocation() {
        return defaultMailboxLocation;
    }

    public void setDefaultMailboxLocation(@Nullable Location defaultMailboxLocation) {
        this.defaultMailboxLocation = defaultMailboxLocation;
        save();
    }

    public String getActiveDatabase() { return activeDatabase; }

    public void setActiveDatabase(String activeDatabase) {
        this.activeDatabase = activeDatabase;
        save();
    }

    public synchronized void save() {
        Bukkit.getScheduler().runTaskAsynchronously(MailMe.getInstance(), () -> {
            MailMe.getInstance().getCache().getFileUtil().save(this);
        });
    }
}
