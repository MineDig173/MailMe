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
        // Things to consider before implementing -> Will it affect version upgrades in future e.g 1.16 -> 1.17. Netty protocol changes, will json?
//        if (value.getType().equals(Material.WRITTEN_BOOK) && value.hasItemMeta() && value.getItemMeta() instanceof BookMeta) {
//            BookMeta bookMeta = (BookMeta) value.getItemMeta();
//            for (int i = 1; i <= bookMeta.getPageCount(); i++) {
//                int in = 0;
//                for (BaseComponent component : bookMeta.spigot().getPage(i)) {
//                    System.out.println(component.toLegacyText());
//                    c.set("page." + i + "." + in, component.toLegacyText());
//                    in++;
//                }
//            }
//        }


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
            ItemStack stack = c.getItemStack("is");
            if (stack == null) return new ItemStack(Material.STONE);
            return stack;
        } catch (InvalidConfigurationException e) {
            return new ItemStack(Material.STONE);
        }
    }
}