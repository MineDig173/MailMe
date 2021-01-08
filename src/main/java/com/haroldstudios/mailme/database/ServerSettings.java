package com.haroldstudios.mailme.database;

import com.haroldstudios.mailme.MailMe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.io.File;


public class ServerSettings {

    @Nullable private Location defaultMailboxLocation;

    @Nullable public Location getDefaultMailboxLocation() {
        return defaultMailboxLocation;
    }

    public void setDefaultMailboxLocation(@Nullable Location defaultMailboxLocation) {
        this.defaultMailboxLocation = defaultMailboxLocation;
        save();
    }

    public synchronized void save() {
        Bukkit.getScheduler().runTaskAsynchronously(MailMe.getInstance(), () -> {
            MailMe.getInstance().getCache().getFileUtil().save(this);
        });
    }
}
