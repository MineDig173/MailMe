package com.haroldstudios.mailme.components.hooks;

import org.bukkit.Location;

public interface HologramHook {

    void addTempHologram(Location loc, String playerName);
    void removeTempHologram(Location loc);
}
