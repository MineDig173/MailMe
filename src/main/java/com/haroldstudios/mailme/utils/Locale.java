package com.haroldstudios.mailme.utils;

import com.haroldstudios.mailme.MailMe;
import me.mattstudios.mfgui.gui.components.ItemBuilder;
import me.mattstudios.mfgui.gui.components.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static com.haroldstudios.mailme.utils.Utils.colour;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Locale {

    //          TOKEN       YAML
    private final Map<String, YamlConfiguration> languagesMap = new HashMap<>();
    private final String serverLangToken;
    private final MailMe plugin;

    public Locale(final MailMe plugin) {

        this.plugin = plugin;

        serverLangToken = plugin.getConfig().getString("lang");

        File folder = new File(plugin.getDataFolder() + "/languages");
        folder.mkdir();
        // Save all preset resources here
        if (!new File(MailMe.getInstance().getDataFolder(), "languages/EN.yml").exists()) {
            plugin.saveResource("languages/EN.yml", false);
        }
        
        for (File lang : folder.listFiles()) {
            
            String fileName = lang.getName();

            File langFile = new File(MailMe.getInstance().getDataFolder() + "/languages/" + fileName);
            YamlConfiguration externalYamlConfig = YamlConfiguration.loadConfiguration(langFile);

            InputStream resource = MailMe.getInstance().getResource("languages/" + fileName);
            if (resource != null) {
                InputStreamReader defConfigStream = new InputStreamReader(resource, UTF_8);

                YamlConfiguration internalLangConfig = YamlConfiguration.loadConfiguration(defConfigStream);


                // Gets all the keys inside the internal file and iterates through all of it's key pairs

                for (String string : internalLangConfig.getKeys(true)) {
                    // Checks if the external file contains the key already.
                    if (!externalYamlConfig.contains(string)) {
                        // If it doesn't contain the key, we set the key based off what was found inside the plugin jar
                        externalYamlConfig.set(string, internalLangConfig.get(string));
                    }
                }

                try {
                    externalYamlConfig.save(langFile);
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }

            this.languagesMap.put(fileName.replace(".yml", "").toUpperCase(), externalYamlConfig);
        }
    }

    public String getMessage(CommandSender sender, String string) {
        return getMessage(getLanguageTokenFor(sender), string);
    }

    public String getMessage(String string) {
        return getMessage(serverLangToken, string);
    }

    public String getMessage(String token, String string) {
        String msg = this.languagesMap.getOrDefault(token, languagesMap.get(serverLangToken)).getString(string);
        if (msg == null) {
            return "Unknown Message! Please contact an administrator. Token: " + string;
        }
        return colour(msg);
    }

    public String[] getMessages(CommandSender sender, String string) {
        return getMessages(getLanguageTokenFor(sender), string);
    }

    public void trySendMessageToUUID(UUID uuid, String msg) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return;

        p.sendMessage(getMessage(p, msg));
    }

    public String[] getMessages(String string) {
        return getMessages(serverLangToken, string);
    }

    public String[] getMessages(String token, String string) {
        List<String> messages = this.languagesMap.getOrDefault(token, languagesMap.get(serverLangToken)).getStringList(string);
        if (messages.isEmpty()) {
            return new String[]{ChatColor.DARK_RED + "Unknown Message! Please contact an administrator. Token: " + string};
        }
        return Utils.colourList(messages).toArray(new String[0]);
    }

    public Set<String> getLanguageTokens() {
        return languagesMap.keySet();
    }

    public String getLanguageTokenFor(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return serverLangToken;
        }

        Player player = (Player) sender;
        String languageToken = MailMe.getInstance().getCache().getPlayerSettings(player).getLanguageToken();

        if (!languageExists(languageToken))
            return serverLangToken;
        else
            return languageToken;
    }

    public boolean languageExists(@Nullable String token) {
        if (token == null) return false;
        File folder = new File(plugin.getDataFolder() + "/languages");
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            plugin.getLogger().severe("Could not find languages folder! What the f**k did you do!!");
            return false;
        }

        return Arrays.stream(listOfFiles).anyMatch(file -> file.getName().startsWith(token.toUpperCase()));
    }

    public ItemStack getItemStack(String path) { return getItemStack(serverLangToken, path); }

    public ItemStack getItemStack(Player player, String path) { return getItemStack(getLanguageTokenFor(player), path); }

    public ItemStack getItemStack(String token, String path) {
        ConfigurationSection section = languagesMap.get(token).getConfigurationSection(path);
            XMaterial xMaterial = XMaterial.matchXMaterial(section.getString("material")).orElse(XMaterial.STONE);
            Material material = xMaterial.parseMaterial().orElse(Material.STONE);

            if (material.equals(Material.AIR)) return new ItemStack(Material.AIR);
            ItemBuilder builder = new ItemBuilder(material)
                    .setName(Utils.colour(section.getString("title")))
                    .setLore(Utils.colourList(section.getStringList("lore")).toArray(new String[]{}))
                    .setAmount(section.getInt("amount"))
                    .glow(section.getBoolean("glow"))
                    .addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);

            if (material.equals(XMaterial.PLAYER_HEAD.parseMaterial()))
                builder.setSkullTexture(section.getString("skull-texture"));

            ItemStack stack = builder.build();
            int modelData = section.getInt("custom-model-data");
            if (modelData != 0) {
                ItemMeta meta = stack.getItemMeta();

                meta.setCustomModelData(section.getInt("custom-model-data"));

                stack.setItemMeta(meta);
            }
            return stack;
    }

}
