package com.haroldstudios.mailme.database.json.serializer;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import me.mattstudios.gui.components.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.*;

public final class ItemStackTypeAdapter extends TypeAdapter<ItemStack>{

    @Override
    public void write(JsonWriter out, ItemStack value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        YamlConfiguration c = new YamlConfiguration();
        c.set("is", value);
        out.value(c.saveToString());
    }

    @Override
    public ItemStack read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        YamlConfiguration c = new YamlConfiguration();
        String n = reader.nextString();
        // Verify material type because yaml loading errors of unknown materials cannot be trapped by try clause.
        if (n.contains("type:")) {
            String type = n.substring(n.indexOf("type:") + 6);
            type = type.substring(0, type.indexOf('\n'));
            Material m = XMaterial.matchXMaterial(type).get().parseMaterial();
            if (m == null) {
                return new ItemStack(Material.STONE);
            }

        }
        try {
            c.loadFromString(n);
            return c.getItemStack("is");
        } catch (InvalidConfigurationException e) {
            return new ItemStack(Material.STONE);
        }
    }

}