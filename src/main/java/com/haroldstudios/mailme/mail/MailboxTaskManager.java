package com.haroldstudios.mailme.mail;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerSettings;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class MailboxTaskManager {

    private final MailMe plugin;
    private BukkitTask pingTask;

    public MailboxTaskManager(MailMe plugin) {
        this.plugin = plugin;
    }

    public void beginTasks() {
        pingTask = beginPingMailboxTask();
    }

    public void stopTasks() {
        pingTask.cancel();
    }

    private BukkitTask beginPingMailboxTask() {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location playerLocation = player.getLocation();
                PlayerSettings playerSettings = plugin.getCache().getPlayerSettings(player);
                playerSettings.getMailboxLocations().stream().filter(loc -> loc.distance(playerLocation) <= ConfigValue.MAILBOX_PING_DISTANCE).forEach(mailbox -> {
                    if (!ConfigValue.VALID_MAILBOXES.contains(mailbox.getBlock().getType())) return; // If block is no longer a valid mailbox, do not ping it. (e.g someone moved it with a piston / got destroyed)
                    plugin.getPlayerMailDAO().hasUnreadMail(player.getUniqueId()).thenAccept(hasUnread -> {
                        if (hasUnread)
                            Utils.playNoteEffect(player, mailbox);
                    });
                });
            }
        }, 0L, 80L);

    }
}
