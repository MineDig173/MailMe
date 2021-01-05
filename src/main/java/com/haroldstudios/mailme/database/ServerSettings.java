package com.haroldstudios.mailme.database;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;


public class ServerSettings {

    @Nullable private Location defaultMailboxLocation;

    @Nullable public Location getDefaultMailboxLocation() {
        return defaultMailboxLocation;
    }

    public void setDefaultMailboxLocation(@Nullable Location defaultMailboxLocation) {
        this.defaultMailboxLocation = defaultMailboxLocation;
    }
}
