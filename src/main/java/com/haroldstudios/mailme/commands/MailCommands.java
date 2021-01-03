package com.haroldstudios.mailme.commands;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerSettings;
import com.haroldstudios.mailme.utils.Locale;
import me.mattstudios.mf.annotations.Alias;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("mail")
@Alias("mailme")
public class MailCommands extends CommandBase {

    private final MailMe plugin;
    private final Locale locale;

    public MailCommands(final MailMe plugin) {
        this.plugin = plugin;
        this.locale = plugin.getLocale();
    }

    @SubCommand("help")
    public void help(CommandSender sender) {
        sender.sendMessage(locale.getMessages(sender, "cmd.help"));
    }

    @SubCommand("lang")
    public void setLanguage(Player player, String lang) {

        if (!locale.languageExists(lang)) {
            locale.getMessage(player, "cmd.lang.failure");
            return;
        }

        PlayerSettings playerSettings = plugin.getCache().getPlayerSettings(player);
        playerSettings.setLanguageToken(lang);
        locale.getMessage(player, "cmd.lang.success");
    }

    @SubCommand("notify")
    @Alias("notifications")
    public void setNotificationSettings(Player player, Boolean bool) {
        PlayerSettings playerSettings = plugin.getCache().getPlayerSettings(player);
        playerSettings.setReceivingNotifications(bool);
        player.sendMessage(locale.getMessage(player, "cmd.notify"));
    }
}
