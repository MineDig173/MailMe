package com.haroldstudios.mailme.utils;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailType;
import me.mattstudios.gui.components.util.ItemBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    public static String colour(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> colourList(List<String> string) {
        return string.stream().map(Utils::colour).collect(Collectors.toList());
    }

    // Gets display name or item name if not present
    public static String getItemName(ItemStack item) {
        String itemName = item.getType().toString();
        if (item.getItemMeta() != null) {
            itemName = item.getItemMeta().getDisplayName().equals("") ? item.getType().toString() : item.getItemMeta().getDisplayName();
        }
        return itemName;
    }

    public static boolean hasSpaceInInventory(List<ItemStack> items, Inventory inventory) {
        int freeSlots = 0;
        for (ItemStack it : inventory.getContents()) {
            if (it == null) continue;
            freeSlots++;
        }
        // Inventory is size 36
        freeSlots = 36 - freeSlots;
        return freeSlots >= items.size();
    }

    // Gives the player an item, if their inventory is full it drops it on the floor.
    public static void giveItem(Player player, ItemStack itemStack) {
        Bukkit.getScheduler().runTask(MailMe.getInstance(), () -> {
            final Map<Integer, ItemStack> map = player.getInventory().addItem(itemStack);
            for (final ItemStack item : map.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        });
    }

    public static ItemStack getItemFromBuilder(Mail.Builder<?> builder, Player player) {
        return getItemFromMail(builder.build(), player);
    }

    public static ItemStack getItemFromMail(Mail mail, Player player) {
        String dir = "gui.revealed-contents";
        Locale locale = MailMe.getInstance().getLocale();

        return ItemBuilder.from(mail.getIcon())
                .glow(true)
                .setName(applyPlaceholders(locale.getMessage(player, dir + ".title"), mail, player)[0])
                .setLore(Arrays.asList(applyPlaceholders(locale.getMessages(player, dir + ".lore"), mail, player)))
                .build();
    }

    public static String getDateFromMs(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        Date date = calendar.getTime();

        SimpleDateFormat format1 = new SimpleDateFormat(ConfigValue.DATE_FORMAT);

        return format1.format(date);
    }

    public static String getTimeFromMS(long millis) {
        if ((TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))) < 0) {
            millis = 0;
        }

        return String.format("%02dd:%02dh:%02dm", TimeUnit.MILLISECONDS.toDays(millis),
                TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis)),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
    }

    private static String[] applyPlaceholders(String[] list, Mail builder, Player player) {
        String[] strings = new String[0];
        for (String each : list) {
            strings = Stream.of(strings, applyPlaceholders(each, builder, player)).flatMap(Stream::of)
                    .toArray(String[]::new);
        }
        return strings;
    }

    public static String[] applyPlaceholders(String string, Mail mail, Player player) {
        string = string.replace("%type%", MailType.getLanguageType(MailType.getMailTypeFromMail(mail), player));
        string = string.replace("%sender%", mail.getSender());
        string = string.replace("%expiry%", getTimeFromMS((mail.getDateReceived() - System.currentTimeMillis() + mail.getExpiryTimeMilliSeconds())));
        string = string.replace("%date%", getDateFromMs(mail.getDateReceived()));
        string = string.replace("%player_name%", player.getName());
        string = string.replace("%archived%", mail.isArchived() + "");

        String[] str;
        if (string.contains("%contents%")) {
            String[] contents = mail.getContentsAsString();
            for (int i = 0; i < contents.length; i++) {
                contents[i] = contents[i].replace("%player_name%", player.getName());
            }
            str = new String[contents.length + 1];

            string = string.replace("%contents%", "");
            str[0] = string;

            System.arraycopy(contents, 0, str, 1, contents.length);

        } else {
            str = new String[]{string};
        }

        return str;
    }

    public static int getAllowedAmountOfMailboxes(final Player player) {
        int currentAmount = 0;
        for (PermissionAttachmentInfo effectivePermission : player.getEffectivePermissions()) {
            String permission = effectivePermission.getPermission();

            if (!permission.startsWith(PermissionConstants.NUM_OF_MAILBOXES)) continue;
            int length = PermissionConstants.NUM_OF_MAILBOXES.length();

            if (permission.length() <= length) continue;
            try {
                int members = Integer.parseInt(permission.substring(length));
                if (members > currentAmount) {
                    currentAmount = members;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        }
        return currentAmount;
    }

    public static void playNoteEffect(Player player, Location location) {
        double note = 6 / 24D; // 6 is the value of the red note
        Location l = location.clone();
        l.add(0.5, 1.2, 0.5);
        player.spawnParticle(Particle.NOTE, l, 1, note, 0, 0, 1);
    }

    public static void particleTower(final Player player, final Location location) {
        Location loc = location.clone();
        loc.add(0.5, 0, 0.5);
        for (int i = 0; i < 20; i++) {
            loc.add(0, 1, 0);
            player.spawnParticle(Particle.VILLAGER_HAPPY, loc, 1);
        }
    }

    public static boolean passedPermissionCheck(Player player, String perm) {
        if (player.hasPermission(perm)) return true;
        String message = MailMe.getInstance().getLocale().getMessage(player, "cmd.no-permission");
        message = message.replace("@perm", perm);
        player.sendMessage(message);
        return false;
    }

}