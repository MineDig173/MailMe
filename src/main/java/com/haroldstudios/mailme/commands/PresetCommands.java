package com.haroldstudios.mailme.commands;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.conversations.LetterInputPrompt;
import com.haroldstudios.mailme.gui.ChooseMailTypeGui;
import com.haroldstudios.mailme.gui.IconSelectorGui;
import com.haroldstudios.mailme.gui.ItemInputGui;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailItems;
import com.haroldstudios.mailme.mail.MailMessage;
import com.haroldstudios.mailme.utils.PermissionConstants;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import me.mattstudios.msg.bukkit.BukkitMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Command("mailpreset")
public class PresetCommands extends CommandBase {

    private final MailMe plugin;

    public PresetCommands(final MailMe plugin) {
        this.plugin = plugin;
    }

    @Default
    @Permission(PermissionConstants.ADMIN)
    public void preset(CommandSender sender) {
        help(sender);
    }

    @SubCommand("help")
    @Permission(PermissionConstants.ADMIN)
    public void help(CommandSender sender) {
        sender.sendMessage(plugin.getLocale().getMessage(sender, "preset-help"));
    }

    @SubCommand("list")
    @Permission(PermissionConstants.ADMIN)
    public void list(CommandSender sender) {
        String msg = plugin.getLocale().getMessage("cmd.identifiers");
        plugin.getPlayerMailDAO().getPresetMailIdentifiers().thenAccept(identifiers -> sender.sendMessage(msg.replace("@identifiers", identifiers.toString())));
    }

    @SubCommand("create")
    @Permission(PermissionConstants.ADMIN)
    public void create(Player player) {
        ChooseMailTypeGui gui = new ChooseMailTypeGui(plugin, player, null, null);
        gui.withRunnable(() -> {
            plugin.getCache().getPresetMailBuilders().put(player.getUniqueId(), gui.getBuilder());
            gui.getGui().close(player);
            edit(player, new String[0]);
        });
        gui.open();

    }

    @SubCommand("delete")
    @Permission(PermissionConstants.ADMIN)
    public void delete(CommandSender sender, String presetName) {
        plugin.getPlayerMailDAO().deletePresetMail(presetName).thenAccept(success -> {

            if (success) {
                sender.sendMessage(plugin.getLocale().getMessage(sender, "cmd.delete.success"));
            } else {
                sender.sendMessage(plugin.getLocale().getMessage(sender, "cmd.delete.failure"));
            }
        });
    }

    @SubCommand("give")
    @Permission(PermissionConstants.ADMIN)
    public void give(CommandSender sender, String playerName, String preset) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!offlinePlayer.hasPlayedBefore()) {
                    sender.sendMessage(plugin.getLocale().getMessage(sender, "cmd.unknown-player"));
                    return;
                }
                UUID uuid = offlinePlayer.getUniqueId();
                plugin.getPlayerMailDAO().getPresetMail(preset).thenAccept(mail -> {
                    if (mail == null) {
                        sender.sendMessage(plugin.getLocale().getMessage(sender, "cmd.invalid-preset"));
                        return;
                    }
                    List<UUID> recipients = new ArrayList<>();
                    recipients.add(uuid);
                    mail.send(recipients);
                    sender.sendMessage(plugin.getLocale().getMessage(sender, "mail.sent"));
                });
            });
        });
    }

    @SubCommand("complete")
    @Permission(PermissionConstants.ADMIN)
    public void complete(Player player){
        Mail.Builder<?> builder = plugin.getCache().getPresetMailBuilders().get(player.getUniqueId());
        if (builder == null) {
            player.sendMessage(plugin.getLocale().getMessage(player, "cmd.must-create-preset-first"));
            return;
        }

        if (builder.getSender() == null || builder.getIdentifier() == null || builder.getIcon() == null || builder.getContents()[0] == null) {
            player.sendMessage(plugin.getLocale().getMessage(player, "cmd.missing-params-preset"));
            return;
        }

        plugin.getPlayerMailDAO().getPresetMail(builder.getIdentifier()).thenAccept(mail -> {
            if (mail != null) {
                player.sendMessage(plugin.getLocale().getMessage("cmd.id-taken"));
                return;
            }
            plugin.getPlayerMailDAO().savePreset(builder.build()).thenAccept(bool -> {
                if (bool) {
                    player.sendMessage(plugin.getLocale().getMessage("cmd.preset-saved"));
                    return;
                }
                player.sendMessage(plugin.getLocale().getMessage("cmd.preset-error"));
            });

        });
    }

    @SubCommand("edit")
    @Permission(PermissionConstants.ADMIN)
    public void edit(Player player, String[] args) {
        Mail.Builder<?> builder = plugin.getCache().getPresetMailBuilders().get(player.getUniqueId());
        if (builder == null) {
            player.sendMessage(plugin.getLocale().getMessage(player, "cmd.must-create-preset-first"));
            return;
        }

        if (args.length <= 1) {
            final BukkitMessage parser = BukkitMessage.create();
            String[] messages = plugin.getLocale().getMessages("preset-creator");
            for (String msg : messages) {
                parser.parse(msg).sendMessage(player);
            }
            return;
        }

        if (args[1].startsWith("id:")) {
            String identifier = args[1].split(":")[1];
            builder.setIdentifier(identifier);
            edit(player, new String[0]);
            return;
        } else if (args[1].startsWith("sender:")) {
            String sender = args[1].split(":")[1];
            builder.setSender(sender);
            edit(player, new String[0]);
            return;
        } else if (args[1].startsWith("expiry:")) {
            String expiry = args[1].split(":")[1];
            int expire;
            try {
                expire = Integer.parseInt(expiry);
            } catch (NumberFormatException e) {
                // must be a number;
                return;
            }
            builder.setExpiryTimeMins(expire);
        } else if (args[1].equalsIgnoreCase("icon")){
            IconSelectorGui gui = new IconSelectorGui(plugin, player, null, builder);
            gui.withRunnable(() -> {
                gui.getGui().close(player);
                player.performCommand("mailpreset edit");
            });
            gui.open();
        } else if (args[1].equalsIgnoreCase("contents")) {
            if (builder instanceof MailItems.Builder) {
                ItemInputGui gui = new ItemInputGui(plugin, player, null, builder, new ArrayList<>());
                gui.withRunnable(() -> {
                    gui.getGui().close(player);
                    player.performCommand("mailpreset edit");
                });
                gui.open();
            } else if (builder instanceof MailMessage.Builder) {
                LetterInputPrompt.begin(plugin, builder, player, () -> player.performCommand("mailpreset edit"));
            }
        } else {
            player.sendMessage(plugin.getLocale().getMessage(player, "cmd.unknown-argument"));
        }
    }

}
