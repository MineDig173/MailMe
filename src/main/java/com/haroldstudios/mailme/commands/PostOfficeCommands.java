package com.haroldstudios.mailme.commands;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.postoffice.PostOffice;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@Command("postoffice")
@Alias("po")
public class PostOfficeCommands extends CommandBase {

    private final MailMe plugin;

    public PostOfficeCommands(final MailMe plugin) {
        this.plugin = plugin;
    }

    @Default
    public void postOffice(CommandSender sender) {
        sender.sendMessage(plugin.getLocale().getMessages(sender,"postoffice-help"));
    }

    @SubCommand("help")
    public void help(CommandSender sender) {
        postOffice(sender);
    }

    @SubCommand("add")
    public void addPostOffice(Player player, String type) {
        Block b = player.getTargetBlock(null, 5);
        if (b.getType().equals(Material.AIR)) {
            player.sendMessage(plugin.getLocale().getMessage(player,"postoffice.add"));
            return;
        }


        if (type.equals("compose")) {
            plugin.getCache().getPostOfficeStore().addPostOffice(new PostOffice(b.getLocation(), true));
        } else {
            plugin.getCache().getPostOfficeStore().addPostOffice(new PostOffice(b.getLocation(), false));
        }
        player.sendMessage(plugin.getLocale().getMessage(player,"postoffice.add"));

    }

    @SubCommand("remove")
    public void removePostOffice(Player player) {
        Block b = player.getTargetBlock(null, 5);

        List<PostOffice> postOffice = plugin.getCache().getPostOfficeStore().getPostOfficeFromLocation(b.getLocation());

        if (postOffice.isEmpty()) {
            player.sendMessage(plugin.getLocale().getMessage(player,"postoffice.no-po-here"));

        } else {
            for (PostOffice eachPO : postOffice) {
                plugin.getCache().getPostOfficeStore().removePostOffice(eachPO);
                plugin.getCache().getPostOfficeStore().update();
            }
            player.sendMessage(plugin.getLocale().getMessage(player,"postoffice.removed"));
        }

    }

    @SubCommand("removeall")
    public void clearPostOffices(Player player) {
        plugin.getCache().getPostOfficeStore().getPostOffices().clear();
        plugin.getCache().getPostOfficeStore().update();

        player.sendMessage(plugin.getLocale().getMessage(player,"postoffice.cleared"));
    }
}
