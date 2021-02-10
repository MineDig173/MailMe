package com.haroldstudios.mailme.listeners;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerSettings;
import com.haroldstudios.mailme.database.json.DataCache;
import com.haroldstudios.mailme.gui.child.ChooseMailTypeGui;
import com.haroldstudios.mailme.gui.child.InboxGui;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailMessage;
import com.haroldstudios.mailme.postoffice.PostOffice;
import com.haroldstudios.mailme.utils.PermissionConstants;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;
import java.util.UUID;

public class EntityEvents implements Listener {

    private final MailMe plugin;

    public EntityEvents(final MailMe plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getPlayerMailDAO().hasUnreadMail(player.getUniqueId()).thenAccept(hasUnread -> {
            if (hasUnread) {
                player.sendMessage(plugin.getLocale().getMessage(player, "cmd.has-unread"));
            }
        });

        if (!player.hasPlayedBefore()) {
            plugin.getPlayerMailDAO().getPresetMail("welcome").thenAccept(mail -> {
                if (mail == null) return;
                mail.sendPreset(player.getUniqueId());
            });
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        DataCache cache = plugin.getCache();
        PlayerSettings playerSettings = cache.getPlayerSettings(player);
        if (!cache.isMailboxAtLocation(block.getLocation()) && !cache.getPostOfficeStore().isPostOfficeAtLocation(block.getLocation())) return;

        event.setCancelled(true);

        if (cache.isMailboxAtLocation(block.getLocation()) && player.hasPermission(PermissionConstants.USE_MAILBOX)) {
            if (playerSettings.getMailboxLocations().contains(block.getLocation())) {
                // Is player's mailbox location
                new InboxGui(plugin, null, InboxGui.getDefaultGuiOptions(player)).open();
            } else {
                // Is someone else's mailbox
                UUID playerWhoOwnsMailbox = cache.getWhoOwnsMailboxAtLocation(block.getLocation());
                if (playerWhoOwnsMailbox == null) return;
                // Generic Builder. Does not matter what type as we combine it anyways upon selection of mail type in gui.
                Mail.Builder<?> builder = new MailMessage.Builder();
                builder.addRecipient(playerWhoOwnsMailbox);
                new ChooseMailTypeGui(plugin, builder, ChooseMailTypeGui.getDefaultGuiOptions(player)).open();
            }
            return;
        }

        Optional<PostOffice> postOfficeOptional = plugin.getCache().getPostOfficeStore().getPostOfficeFromLocation(block.getLocation()).stream().findFirst();
        if (postOfficeOptional.isPresent() && player.hasPermission(PermissionConstants.USE_POSTOFFICE)) {
            PostOffice postOffice = postOfficeOptional.get();

            if (postOffice.isSendType()) {
                plugin.getMailCommandHandler().compose(player);
            } else {
                new InboxGui(plugin, null, InboxGui.getDefaultGuiOptions(player)).open();
            }
        }
    }
}