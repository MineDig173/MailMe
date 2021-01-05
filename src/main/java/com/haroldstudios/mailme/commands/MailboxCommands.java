package com.haroldstudios.mailme.commands;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.conversations.PlayerSearch;
import com.haroldstudios.mailme.database.PlayerSettings;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.mf.annotations.Alias;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("mailbox")
public class MailboxCommands extends CommandBase {

    private final MailMe plugin;

    public MailboxCommands(MailMe plugin) {
        this.plugin = plugin;
    }

    @Default
    public void mailbox(CommandSender sender) {

    }

    @SubCommand("help")
    public void help(CommandSender sender) {

    }

    @SubCommand("add")
    public void addMailbox(Player player) {

        if (plugin.getCache().getPlayerSettings(player).getMailboxLocations().size() >= Utils.getAllowedAmountOfMailboxes(player)) {
            player.sendMessage(plugin.getLocale().getMessage("mailbox.limit-reached"));
            return;
        }

        Block block = player.getTargetBlock(null, 6);
        if (!ConfigValue.VALID_MAILBOXES.contains(block.getType())) {
            player.sendMessage(plugin.getLocale().getMessage("mailbox.invalid-item"));
            return;
        }

        if (plugin.getCache().isMailboxAtLocation(block.getLocation())) {
            player.sendMessage(plugin.getLocale().getMessage("mailbox.location-occupied"));
            return;
        }

        plugin.getCache().getPlayerSettings(player).addMailboxLocation(block.getLocation());
        player.sendMessage(plugin.getLocale().getMessage("mailbox.placement-success"));
    }

    @SubCommand("remove")
    @Alias("delete")
    public void removeMailbox(Player player) {
        Block block = player.getTargetBlock(null, 6);
        PlayerSettings playerSettings = plugin.getCache().getPlayerSettings(player);

        if (!playerSettings.getMailboxLocations().contains(block.getLocation())) {
            player.sendMessage(plugin.getLocale().getMessage("mailbox.no-mailbox-here"));
            return;
        }

        playerSettings.removeMailboxLocation(block.getLocation());
        player.sendMessage(plugin.getLocale().getMessage("mailbox.location-removed"));
    }
}
