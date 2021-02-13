package com.haroldstudios.mailme.database.json.serializer;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.haroldstudios.mailme.MailMe;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Vector;

public class MailTypeAdapterFactory implements com.google.gson.TypeAdapterFactory {

    MailMe plugin;

    /**
     * @param plugin plugin
     */
    public MailTypeAdapterFactory(MailMe plugin) {
        this.plugin = plugin;
    }

    /* (non-Javadoc)
     * @see com.google.gson.TypeAdapterFactory#create(com.google.gson.Gson, com.google.gson.reflect.TypeToken)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<?> rawType = type.getRawType();
        if (ItemStack.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new ItemStackTypeAdapter();
        } else if (PotionEffectType.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new PotionTypeAdapter();
        } else if (World.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new WorldTypeAdapter();
        } else if (Vector.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new VectorTypeAdapter();
        } else if (Location.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new LocationTypeAdapter();
        } else if (ConfigurationSerializable.class.isAssignableFrom(rawType)) {
            // This covers a lot of Bukkit objects
            return (TypeAdapter<T>) new BukkitObjectTypeAdapter(gson.getAdapter(Map.class));
        }
        return null;
    }
}