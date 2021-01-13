package com.haroldstudios.mailme.commands;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerSettings;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.PermissionConstants;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

@Command("mailbox")
public class MailboxCommands extends CommandBase {

    private final MailMe plugin;

    public MailboxCommands(MailMe plugin) {
        this.plugin = plugin;
    }

    @Default
    @Permission(PermissionConstants.MAILBOX_HELP)
    public void mailbox(CommandSender sender) {
        help(sender);
    }

    @SubCommand("help")
    @Permission(PermissionConstants.MAILBOX_HELP)
    public void help(CommandSender sender) {
        sender.sendMessage(plugin.getLocale().getMessages(sender, "mailbox-help"));
    }

    @SubCommand("add")
    @Permission(PermissionConstants.ADD_MAILBOX)
    public void addMailbox(Player player) {

        if (plugin.getCache().getPlayerSettings(player).getMailboxLocations().size() >= Utils.getAllowedAmountOfMailboxes(player)) {
            player.sendMessage(plugin.getLocale().getMessage(player,"mailbox.limit-reached"));
            return;
        }
        // Mailbox adder checker
        if (MailMe.getInstance().getVaultHook() != null && !MailMe.getInstance().getVaultHook().attemptTransaction(player, plugin.getConfig().getDouble("cost.set-mailbox"))) {
            return;
        }

        Block block = player.getTargetBlock((Set<Material>) null, 6);
        if (!ConfigValue.VALID_MAILBOXES.contains(block.getType())) {
            player.sendMessage(plugin.getLocale().getMessage(player,"mailbox.invalid-item"));
            return;
        }

        if (plugin.getCache().isMailboxAtLocation(block.getLocation())) {
            player.sendMessage(plugin.getLocale().getMessage(player,"mailbox.location-occupied"));
            return;
        }

        // mail.anonymous

        plugin.getCache().getPlayerSettings(player).addMailboxLocation(block.getLocation());
        player.sendMessage(plugin.getLocale().getMessage(player,"mailbox.placement-success"));
    }

    @SubCommand("remove")
    @Alias("delete")
    @Permission(PermissionConstants.REMOVE_MAILBOX)
    public void removeMailbox(Player player) {
        Block block = player.getTargetBlock(null, 6);
        PlayerSettings playerSettings = plugin.getCache().getPlayerSettings(player);

        if (!playerSettings.getMailboxLocations().contains(block.getLocation())) {
            player.sendMessage(plugin.getLocale().getMessage(player,"mailbox.no-mailbox-here"));
            return;
        }

        playerSettings.removeMailboxLocation(block.getLocation());
        player.sendMessage(plugin.getLocale().getMessage(player,"mailbox.location-removed"));
    }

    @SubCommand("find")
    @Alias("highlight")
    @Permission(PermissionConstants.FIND_NEAR_MAILBOXES)
    public void findMailboxes(Player player) {
        List<Location> mailboxes = plugin.getCache().getMailboxes();
        mailboxes.add(plugin.getCache().getServerSettings().getDefaultMailboxLocation());
        Location playerLocation = player.getLocation();

        player.sendMessage(plugin.getLocale().getMessage(player, "mailbox.highlighted"));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Location mailbox : mailboxes) {
                if (mailbox.distance(playerLocation) > ConfigValue.MAILBOX_FIND_DISTANCE) continue;
                Utils.particleTower(player, mailbox);
            }
        });
    }

    @SubCommand("defaultmb")
    @Alias("setdefault")
    @Permission(PermissionConstants.ADMIN)
    public void setDefault(Player player) {
        Block block = player.getTargetBlock(null, 7);
        plugin.getCache().getServerSettings().setDefaultMailboxLocation(block.getLocation());
        player.sendMessage(plugin.getLocale().getMessage(player, "cmd.default-mb-set"));
    }
}
