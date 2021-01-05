package com.haroldstudios.mailme.utils;

import com.haroldstudios.mailme.MailMe;
import me.mattstudios.mfgui.gui.components.ItemBuilder;
import me.mattstudios.mfgui.gui.components.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigValue {

    public static List<ItemStack> ICON_LIST = new ArrayList<>();
    public static List<Material> VALID_MAILBOXES = new ArrayList<>();
    public static int MAILBOX_PING_DISTANCE = 50;

    private ConfigValue() {}

    public static void load(MailMe plugin) {
        FileConfiguration config = plugin.getConfig();

        List<String> section = config.getStringList("icons");
        section.forEach(icon -> {
            if (!icon.contains(";")) {
                icon = icon + ";0";
            }
            ItemStack stack = ItemBuilder.from(XMaterial.valueOf(icon.split(";")[0]).parseItem()).glow(true).build();
            if (Integer.parseInt(icon.split(";")[1]) != 0) {
                ItemMeta meta = stack.getItemMeta();
                meta.setCustomModelData(Integer.valueOf(icon.split(";")[1]));
                meta.setUnbreakable(true);
                stack.setItemMeta(meta);
            }
            ICON_LIST.add(stack);
        });

        config.getStringList("valid-mailboxes").forEach(element -> {
            Optional<Material> xMaterial = XMaterial.valueOf(element).parseMaterial();
            xMaterial.ifPresent(material -> VALID_MAILBOXES.add(material));
        });

        MAILBOX_PING_DISTANCE = config.getInt("mailbox-ping-distance");
    }
}
