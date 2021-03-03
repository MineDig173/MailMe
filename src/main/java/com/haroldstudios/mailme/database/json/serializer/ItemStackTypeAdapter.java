package com.haroldstudios.mailme.database.json.serializer;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.haroldstudios.mailme.MailMe;
import me.mattstudios.gui.components.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;

public final class ItemStackTypeAdapter extends TypeAdapter<ItemStack>{

    @Override
    public void write(JsonWriter out, ItemStack value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.value(itemStackToBase64(value));
    }

    @Override
    public ItemStack read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }

        String n = reader.nextString();

        try {
            ItemStack stack = itemStackFromBase64(n);
            if (stack != null) {
                return stack;
            }
        } catch (Exception ex) {
            MailMe.debug(ItemStackTypeAdapter.class, "Tried to use Base64 Reader. Failed. (May not be an error on legacy mails using old serialiser)");
        }

        YamlConfiguration c = new YamlConfiguration();
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

    public static String itemStackToBase64(ItemStack item) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Encoder BASE64 Unable to save item stacks.", e);
        }
    }

    public static ItemStack itemStackFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack item = (ItemStack) dataInput.readObject();

            dataInput.close();
            return item;
        } catch (ClassNotFoundException e) {
            throw new IOException("Encoder BASE64 Unable to decode class type.", e);
        }
    }
}