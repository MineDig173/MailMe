package com.haroldstudios.mailme.utils;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static String colour(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> colourList(List<String> string) {
        return string.stream().map(Utils::colour).collect(Collectors.toList());
    }

}
