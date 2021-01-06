package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.conversations.LetterInputPrompt;
import com.haroldstudios.mailme.conversations.PlayerSearch;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailBook;
import com.haroldstudios.mailme.mail.MailItems;
import com.haroldstudios.mailme.mail.MailMessage;
import me.mattstudios.mfgui.gui.components.ItemBuilder;
import me.mattstudios.mfgui.gui.components.xseries.XMaterial;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipientSelectorGui extends AbstractScrollingMailGui {

    private final List<OfflinePlayer> playerList;

    public RecipientSelectorGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder) {
        super(plugin, player, previousMenu, 6,  plugin.getLocale().getMessage(player, "gui.titles.recipient-selector"), builder);

        getGui().setItem(4,4, new GuiItem(getPlugin().getLocale().getItemStack(player,"gui.search-players"), event -> PlayerSearch.begin(plugin, builder, player)));
        getGui().setItem(4,6, new GuiItem(getPlugin().getLocale().getItemStack(player,"gui.remove-filters"), event -> {
            new RecipientSelectorGui(getPlugin(), getPlayer(), getPreviousMenu(), getBuilder()).open();
            playUISound();
        }));
        getGui().setItem(4,5, getExpandItem()); //todo event action
        getGui().setItem(6,9, new GuiItem(getNextMenuButton(), event -> {
            playUISound();
            if (getBuilder().getRecipients().size() > 0) {
                next();
            }
        }));
        this.playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public RecipientSelectorGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder, OfflinePlayer p) {
        this(plugin, player, previousMenu, builder);
        this.playerList.clear();
        this.playerList.add(p);
    }

    @Override
    public void open() {
        // Add current recipients to front of menu
        getBuilder().getRecipients().forEach(recipient -> getItems().add(getItemForPlayer(Bukkit.getOfflinePlayer(recipient))));

        for (OfflinePlayer p : playerList) {
            // Because we add our recipients to front of menu already
            if (getBuilder().isRecipient(p.getUniqueId())) continue;
            //if (getPlayer().equals(p)) continue; //todo
           getItems().add(getItemForPlayer(p));
        }

        getGui().addItem(getItems().toArray(new GuiItem[0]));
        getGui().open(getPlayer());
    }

    private GuiItem getItemForPlayer(OfflinePlayer p) {
        ItemBuilder itemBuilder = ItemBuilder.from(XMaterial.PLAYER_HEAD.parseItem()).setName(ChatColor.RED+ p.getName()).setSkullOwner(p);
        if (getBuilder().isRecipient(p.getUniqueId())) {
            itemBuilder.glow(true).setName(ChatColor.GREEN + (p.getName() == null ? "?" : p.getName()));
        }
        return itemBuilder.asGuiItem(event -> {
            playUISound();
            if (getBuilder().isRecipient(p.getUniqueId())) {
                getBuilder().removeRecipient(p.getUniqueId());
            } else {
                getBuilder().addRecipient(p.getUniqueId());
            }
            getGui().updatePageItem(event.getSlot(), getItemForPlayer(p));
        });
    }

    @Override
    void nextMenu() {
        if (getBuilder() instanceof MailMessage.Builder) {
            getGui().close(getPlayer());
            LetterInputPrompt.begin(getPlugin(), getBuilder(), getPlayer());
        } else if (getBuilder() instanceof MailItems.Builder) {
            new ItemInputGui(getPlugin(), getPlayer(), this, getBuilder(), new ArrayList<>()).open();
        } else if (getBuilder() instanceof MailBook.Builder) {
            new ClickToSendGui(getPlugin(), getPlayer(), this, getBuilder()).open();
        }
    }
}
