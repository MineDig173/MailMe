package com.haroldstudios.mailme.database.json.serializer;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.haroldstudios.mailme.MailMe;
import me.mattstudios.gui.components.xseries.XMaterial;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemStackTypeAdapter extends TypeAdapter<ItemStack>{

    @Override
    public void write(JsonWriter out, ItemStack value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        YamlConfiguration c = new YamlConfiguration();
//        if (value.getType().equals(Material.WRITTEN_BOOK)) {
//            if (value.hasItemMeta()) {
//                BookMeta bookMeta = (BookMeta) value.getItemMeta();
//                if (bookMeta != null) {
//                    for (int i = 1; i <= bookMeta.getPageCount(); i++) {
//                        int in = 0;
//                        for (BaseComponent component : bookMeta.spigot().getPage(i)) {
//                            System.out.println(component.toLegacyText());
//                            c.set("page." + i + "." + in, component.toLegacyText());
//                            in++;
//                        }
//                    }
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
        // ARROW CODE WOOOO
        try {
            c.loadFromString(n);
            ItemStack stack = c.getItemStack("is");
            if (stack == null) return new ItemStack(Material.STONE);
//            if (stack.getType().equals(Material.WRITTEN_BOOK)) {
//                if (stack.hasItemMeta()) {
//                    BookMeta bookMeta = (BookMeta) stack.getItemMeta();
//                    if (bookMeta != null) {
//                        if (c.getConfigurationSection("page") != null) {
//                            for (String key : c.getConfigurationSection("page").getKeys(false)) {
//                                int page;
//                                try {
//                                    page = Integer.parseInt(key);
//                                } catch (NumberFormatException ex) {
//                                    MailMe.debug(ex);
//                                    continue;
//                                }
//
//                                List<BaseComponent> componentList = new ArrayList<>();
//                                for (String key2 : c.getConfigurationSection("page." + page).getKeys(false)) {
//                                    System.out.println(c.getString(c.getString("page." + key + "." + key2)));
//                                    componentList.addAll(Arrays.asList(toBaseComponent(c.getString("page." + key + "." + key2))));
//                                }
//                                bookMeta.spigot().setPage(page, componentList.toArray(new BaseComponent[0]));
//                            }
//                            stack.setItemMeta(bookMeta);
//                        }
//                    }
//                }
//
//            }
            return stack;
        } catch (InvalidConfigurationException e) {
            return new ItemStack(Material.STONE);
        }
    }

    public static BaseComponent[] toBaseComponent(String json) {
        // Remove the json identifier prefix
        json = json.replace("[JSON]", "");

        // Parse it
        return ComponentSerializer.parse(json);
    }

}