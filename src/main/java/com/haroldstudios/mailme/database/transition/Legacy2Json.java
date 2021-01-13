package com.haroldstudios.mailme.database.transition;

import com.google.gson.*;
import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.json.PlayerDataFile;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailItems;
import com.haroldstudios.mailme.mail.MailMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;

public class Legacy2Json implements Transitionable {

    private final MailMe plugin;

    public Legacy2Json(final MailMe plugin) {
        this.plugin = plugin;
    }

    @Override
    public void transitionMail() {
        File folder = new File(plugin.getDataFolder(), "playerdata");

        if (!folder.exists()) {
            plugin.getLogger().warning("Tried to transition from legacy2latest but could not find the playerdata folder!");
            return;
        }

        Gson gson = plugin.getCache().getFileUtil().getGson();

        JsonObject jsonObject;
        for (File file : folder.listFiles()) {
            try {
                JsonParser parser = new JsonParser();
                JsonElement jsonElement = parser.parse(new FileReader(file));
                jsonObject = jsonElement.getAsJsonObject();
            } catch (IOException e) {
                plugin.getLogger().warning("Couldn't convert file: " + file + " to latest");
                continue;
            }

            String uuidString = FilenameUtils.getBaseName(file.getName());
            UUID fileUuid;
            try {
                fileUuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException ex){
                MailMe.debug(Json2MySQL.class, "Could not convert to uuid: " + uuidString);
                continue;
            }
            PlayerDataFile data = PlayerDataFile.getPlayerDataFile(fileUuid, plugin);
            List<Mail> mailList = new ArrayList<>();

            if (!jsonObject.has("mail")) {
                fail(file.getName() + ":mail");
                continue;
            }

            JsonObject main = jsonObject.getAsJsonObject("mail");

            // Dives and gets keys (and values)
            Set<Map.Entry<String, JsonElement>> entries = main.entrySet();
            for (Map.Entry<String, JsonElement> entry: entries) {

                if (!main.has(entry.getKey())) {
                    fail(file.getName() + ":" + entry.getKey());
                    continue;
                }
                JsonObject dateId = main.getAsJsonObject(entry.getKey());
                if (!dateId.has("type")) {
                    fail(file.getName() + ":type");
                    continue;
                }

                String type = dateId.getAsJsonPrimitive("type").getAsString();
                type = type.substring(type.lastIndexOf(".") + 1);
                MailMe.debug(Legacy2Json.class, "Type: " + type);
                if (!(type.equalsIgnoreCase("MailMessages") || type.equalsIgnoreCase("MailItems"))) {
                    continue;
                }

                if (!dateId.has("data")) {
                    fail(file.getName() + ":data");
                    continue;
                }

                dateId = dateId.getAsJsonObject("data");

                if (!dateId.has("icon")) {
                    fail(file.getName() + ":icon");
                    continue;
                }
                JsonElement icon = dateId.getAsJsonPrimitive("icon");
                ItemStack ic = gson.fromJson(icon, ItemStack.class);

                if (!dateId.has("sender")) {
                    fail(file.getName() + ":sender");
                    continue;
                }

                String senderUuid = dateId.getAsJsonPrimitive("sender").getAsString();
                UUID uuid;
                try {
                    uuid = UUID.fromString(senderUuid);
                } catch (IllegalArgumentException ex) {
                    fail(file.getName() + ":sender uuid was null");
                    continue;
                }
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                String senderName = player.getName() == null ? "?" : player.getName();

                if (!dateId.has("server")) {
                    fail(file.getName() + ":server");
                    continue;
                }

                if (dateId.getAsJsonPrimitive("server").getAsBoolean()) {
                    senderName = "Server";
                }

                if (!dateId.has("anonymous")) {
                    fail(file.getName() + ":anonymous");
                    continue;
                }

                if (dateId.getAsJsonPrimitive("anonymous").getAsBoolean()) {
                    senderName = "Anonymous";
                }

                if (!dateId.has("read")) {
                    fail(file.getName() + ":read");
                    continue;
                }

                boolean read = dateId.getAsJsonPrimitive("read").getAsBoolean();
                Mail mail = null;
                if (type.equalsIgnoreCase("MailMessages")) {

                    if (!dateId.has("message")) {
                        fail(file.getName() + ":message");
                        continue;
                    }

                    String message = dateId.getAsJsonPrimitive("message").getAsString();
                    mail = new MailMessage.Builder().setMessage(message).setIcon(ic).setSender(senderName).build();
                    mail.setRead(read);

                } else if (type.equalsIgnoreCase("MailItems")) {

                    if (!dateId.has("items")) {
                        fail(file.getName() + ":items");
                        continue;
                    }

                    JsonArray itemArray = dateId.getAsJsonArray("items");
                    List<ItemStack> items = new ArrayList<>();
                    for (JsonElement jsonElement : itemArray) {
                        items.add(gson.fromJson(jsonElement, ItemStack.class));
                    }

                    mail = new MailItems.Builder().setSender(senderName).setIcon(ic).setItemStackList(items).build();
                    mail.setRead(read);
                    mail.setLegacy(true);
                    
                }

                if (mail == null) continue;
                mailList.add(mail);
            }
            data.insertMail(mailList);
        }

    }

    private void fail(String sect) {
        plugin.getLogger().warning("Potentially failed to convert section " + sect
         + ". This is not an error!");
    }
}
