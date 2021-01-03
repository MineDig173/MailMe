package com.haroldstudios.mailme.utils;

import com.haroldstudios.mailme.MailMe;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

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
        
        for (File lang : folder.listFiles()) {
            
            String fileName = lang.getName();
            
            if (!new File(MailMe.getInstance().getDataFolder() + "/languages", fileName).exists()) {
                MailMe.getInstance().saveResource("languages/" + fileName + ".yml", false);
            }

            File langFile = new File(MailMe.getInstance().getDataFolder() + "/languages/" + fileName);
            YamlConfiguration externalYamlConfig = YamlConfiguration.loadConfiguration(langFile);

            InputStream resource = MailMe.getInstance().getResource(fileName);
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
        return Utils.colour(msg);
    }

    public String[] getMessages(CommandSender sender, String string) {
        return getMessages(getLanguageTokenFor(sender), string);
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

        if (languageExists(languageToken))
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

}
