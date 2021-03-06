package com.haroldstudios.mailme.commands;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerSettings;
import com.haroldstudios.mailme.database.transition.Json2MySQL;
import com.haroldstudios.mailme.database.transition.Legacy2Json;
import com.haroldstudios.mailme.database.transition.MySQL2Json;
import com.haroldstudios.mailme.gui.child.ChooseMailTypeGui;
import com.haroldstudios.mailme.gui.child.InboxGui;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailMessage;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.Locale;
import com.haroldstudios.mailme.utils.Pagination;
import com.haroldstudios.mailme.utils.PermissionConstants;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command("mail")
@Alias("mailme")
public class MailCommands extends CommandBase {

    private final MailMe plugin;
    private final Locale locale;

    public MailCommands(final MailMe plugin) {
        this.plugin = plugin;
        this.locale = plugin.getLocale();
    }

    @Default
    @Permission(PermissionConstants.READ_MAIL)
    public void mail(CommandSender sender) {
        if (!(sender instanceof Player)) {
            help(sender);
            return;
        }
        read((Player) sender);
    }

    @SubCommand("reload")
    @Permission(PermissionConstants.ADMIN)
    public void reload(CommandSender sender) {
        for (Location loc : plugin.getCache().getMailboxes()) {
            if (plugin.getHologramHook() != null)
                plugin.getHologramHook().removeTempHologram(loc);
        }
        plugin.onDisable();
        plugin.reloadConfig();
        plugin.onEnable();
        sender.sendMessage(plugin.getLocale().getMessage(sender, "cmd.reload"));
    }

    @SubCommand("send")
    @Alias("compose")
    @Permission(PermissionConstants.COMPOSE_MAIL)
    public void compose(Player player) {
        new ChooseMailTypeGui(plugin, null, ChooseMailTypeGui.getDefaultGuiOptions(player)).open();
    }

    @SubCommand("read")
    @Alias("inbox")
    @Permission(PermissionConstants.READ_MAIL)
    public void read(Player player) {
        new InboxGui(plugin, null, InboxGui.getDefaultGuiOptions(player).withReadOnlyMode(ConfigValue.MAILBOX_READ_ONLY)).open();
    }

    @SubCommand("help")
    @Permission(PermissionConstants.MAIL_HELP)
    public void help(CommandSender sender) {
        sender.sendMessage(locale.getMessages(sender, "cmd.help"));
    }

    @SubCommand("lang")
    @Permission(PermissionConstants.MODIFY_LANG)
    @Completion("#locale")
    public void setLanguage(Player player, String lang) {

        if (!locale.languageExists(lang)) {
            player.sendMessage(locale.getMessage(player, "cmd.lang.failure"));
            return;
        }

        PlayerSettings playerSettings = plugin.getCache().getPlayerSettings(player);
        playerSettings.setLanguageToken(lang);
        player.sendMessage(locale.getMessage(player, "cmd.lang.success"));
    }

    @SubCommand("notify")
    @Alias("notifications")
    @Permission({PermissionConstants.MODIFY_NOTIFY})
    public void setNotificationSettings(Player player, Boolean bool) {

        if (bool == null) {
            player.sendMessage(plugin.getLocale().getMessage(player,"cmd.notify.failure"));
            return;
        }
        PlayerSettings playerSettings = plugin.getCache().getPlayerSettings(player);
        playerSettings.setReceivingNotifications(bool);
        player.sendMessage(locale.getMessage(player, "cmd.notify.success"));
    }


    @SubCommand("reply")
    @Permission(PermissionConstants.REPLY_MAIL)
    public void reply(Player player, String pl) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(pl);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!target.hasPlayedBefore()) {
                    player.sendMessage(plugin.getLocale().getMessage(player, "cmd.unknown-player"));
                    return;
                }

                Mail.Builder<?> builder = new MailMessage.Builder();
                builder.addRecipient(target.getUniqueId());
                new ChooseMailTypeGui(plugin, builder, ChooseMailTypeGui.getDefaultGuiOptions(player)).open();
            });
        });
    }


    @SubCommand("text")
    @Permission(PermissionConstants.READ_AS_TEXT)
    public void readAsText(Player player, @Optional Integer pge) {
        if (pge == null) pge = 0;
        final int page = pge;
        plugin.getPlayerMailDAO().getAllMail(player.getUniqueId()).thenAccept(mail -> {
            if (page > 0) {
                TextComponent message = new TextComponent(MailMe.getInstance().getLocale().getMessage(player, "text.prev-page"));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MailMe.getInstance().getLocale().getMessage(player, "text.prev-page-hover")).create()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mailme text " + (page - 1)));
                player.spigot().sendMessage(message);
            }

            Pagination<Mail> pagination = new Pagination<>(2, Arrays.asList(mail));

            if (page < pagination.totalPages()) {
                pagination.getPage(page).forEach(m -> player.spigot().sendMessage(m.getMailAsText(player)));

                TextComponent m = new TextComponent(MailMe.getInstance().getLocale().getMessage(player, "text.next-page"));
                m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MailMe.getInstance().getLocale().getMessage(player, "text.next-page-hover")).create()));
                m.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mailme text " + (page + 1)));
                player.spigot().sendMessage(m);
            }
        });
    }

    @SubCommand("convert")
    @Permission(PermissionConstants.ADMIN)
    public void convertDatabaseStorage(CommandSender sender, String type) {
        if (!(type.equalsIgnoreCase("json2mysql") || type.equals("mysql2json") || type.equals("legacy2json"))) {
            sender.sendMessage("??c??l!!! -> " + ChatColor.GREEN + "json2mysql OR mysql2json OR legacy2json");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Beginning conversion. (Language is English for future debugging purposes)");
        sender.sendMessage(plugin.getLocale().getMessage(sender, "cmd.generic-processing"));
        if (type.equalsIgnoreCase("json2mysql")) {
            sender.sendMessage("Plugin will convert in background. Plugin is safe to use while this process occurs, query times will just be slower. (Language is English for future debugging purposes)");
            new Json2MySQL(plugin).transitionMail();
        } else if (type.equalsIgnoreCase("mysql2json")) {
            new MySQL2Json(plugin).transitionMail();
        } else if (type.equalsIgnoreCase("legacy2json")) {
            new Legacy2Json(plugin).transitionMail();
        }
        sender.sendMessage(plugin.getLocale().getMessage(sender, "cmd.generic-success"));
    }

    @SubCommand("debug")
    @Permission(PermissionConstants.ADMIN)
    public void debug(CommandSender sender) {
        StringBuilder sb = new StringBuilder()
                .append("[MailMe Debug Output]")
                .append("\nBukkit version: ")
                .append(Bukkit.getServer().getBukkitVersion())
                .append("\nPlugin Version: ")
                .append(plugin.getDescription().getVersion())
                .append("\nJava Version: ")
                .append(System.getProperty("java.runtime.version"))
                .append("\nLoaded Language files/tokens: ")
                .append(plugin.getLocale().getLanguageTokens());

        sender.sendMessage(sb.toString());
    }
}