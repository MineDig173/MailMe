package com.haroldstudios.mailme.utils;

import com.haroldstudios.mailme.MailMe;
import me.mattstudios.gui.components.util.ItemBuilder;
import me.mattstudios.gui.components.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ConfigValue {

    public static List<ItemStack> ICON_LIST = new ArrayList<>();
    public static List<Material> VALID_MAILBOXES = new ArrayList<>();
    public static int MAILBOX_PING_DISTANCE = 50;
    public static int MAILBOX_FIND_DISTANCE = 50;
    public static int EXPIRY_TIME_MINS = ((60 * 24) * 30); // 30 days in minutes
    public static double COST_MESSAGE, COST_ITEM, COST_BOOK = 0;
    public static boolean HOOK_VAULT_ENABLED, HOOK_HOLOGRAMS_ENABLED = true;
    public static int SEND_DELAY = 0;
    public static boolean DEBUG = true;
    public static boolean CAN_SEND_TO_SELF = false;
    public static String DATE_FORMAT = "dd-MM-yyyy";
    public static boolean MAILBOX_READ_ONLY = false;
    public static boolean USE_BSTATS = false; // disabled by default if cannot load
    public static boolean SHOULD_MASS_DELETE_UNREADS = true;

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
                stack = ItemBuilder.from(stack).addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE).build();
            }
            ICON_LIST.add(stack);
        });

        config.getStringList("valid-mailboxes").forEach(element -> VALID_MAILBOXES.add(XMaterial.valueOf(element).parseMaterial()));

        MAILBOX_PING_DISTANCE = config.getInt("mailbox-ping-distance");
        MAILBOX_FIND_DISTANCE = config.getInt("mailbox-find-distance");
        EXPIRY_TIME_MINS = config.getInt("expiry-time-mins");
        COST_MESSAGE = config.getDouble("cost.message");
        COST_ITEM = config.getDouble("cost.item");
        COST_BOOK = config.getDouble("cost.book");
        HOOK_HOLOGRAMS_ENABLED = config.getBoolean("hooks.holograms");
        HOOK_VAULT_ENABLED = config.getBoolean("hooks.vault");
        SEND_DELAY = config.getInt("send-delay");
        DEBUG = config.getBoolean("debug");
        CAN_SEND_TO_SELF = config.getBoolean("can-send-to-self");
        DATE_FORMAT = config.getString("date-format");
        MAILBOX_READ_ONLY = config.getBoolean("mailbox-read-only");
        USE_BSTATS = config.getBoolean("enable-bstats");
        SHOULD_MASS_DELETE_UNREADS = config.getBoolean("can-mass-delete-unreads");
    }
}