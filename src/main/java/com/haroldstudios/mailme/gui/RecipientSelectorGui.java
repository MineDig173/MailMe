package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.conversations.ConsoleMailInput;
import com.haroldstudios.mailme.conversations.LetterInputPrompt;
import com.haroldstudios.mailme.conversations.PlayerSearch;
import com.haroldstudios.mailme.mail.*;
import com.haroldstudios.mailme.utils.ConfigValue;
import me.mattstudios.gui.components.util.ItemBuilder;
import me.mattstudios.gui.components.xseries.XMaterial;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipientSelectorGui extends AbstractScrollingMailGui implements Expandable {

    private final List<OfflinePlayer> playerList;

    public RecipientSelectorGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder, GuiType type) {
        super(plugin, player, previousMenu, 6,  plugin.getLocale().getMessage(player, "gui.titles.recipient-selector"), builder, type);

        GuiItem searchPlayers = new GuiItem(getPlugin().getLocale().getItemStack(player,"gui.search-players"), event -> {
            PlayerSearch.begin(plugin, builder, player);
            gracefulExit = true;
            getGui().close(getPlayer());
        });
        GuiItem removeFilter = new GuiItem(getFilterItem(), event -> {
            new RecipientSelectorGui(getPlugin(), getPlayer(), this, getBuilder(), type).open();
            playUISound();
        });
        GuiItem nextMenu = new GuiItem(getNextMenuButton(), event -> {
            playUISound();
            if (getBuilder().getRecipients().size() > 0) {
                next();
            }
        });
        addItem(searchPlayers, getGuiConfig().getItemGContainer("recipient-selector-menu.search-players"), type);
        addItem(removeFilter, getGuiConfig().getItemGContainer("recipient-selector-menu.remove-filter"), type);
        addItem(nextMenu, getGuiConfig().getItemGContainer("recipient-selector-menu.next-menu"), type);
        addExpandableItems(this, type);
        this.playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public RecipientSelectorGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder, GuiType type, OfflinePlayer p) {
        this(plugin, player, previousMenu, builder, type);
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
            if (!ConfigValue.CAN_SEND_TO_SELF) {
                if (getPlayer().equals(p)) continue;
            }
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
            gracefulExit = true;
            getGui().close(getPlayer());
            LetterInputPrompt.begin(getPlugin(), getBuilder(), getPlayer(), null);
        } else if (getBuilder() instanceof MailItems.Builder) {
            new ItemInputGui(getPlugin(), getPlayer(), this, getBuilder(), new ArrayList<>()).open();
        } else if (getBuilder() instanceof MailBook.Builder) {
            new ClickToSendGui(getPlugin(), getPlayer(), this, getBuilder()).open();
        } else if (getBuilder() instanceof MailConsoleCommand.Builder) {
            gracefulExit = true;
            getGui().close(getPlayer());
            ConsoleMailInput.begin(getPlugin(), getBuilder(), getPlayer(), null);
        }
    }

    @Override
    public void expand() {
        new RecipientSelectorGui(getPlugin(), getPlayer(), this, getBuilder(), GuiType.EXPANDED).open();
    }

    @Override
    public void collapse() {
        new RecipientSelectorGui(getPlugin(), getPlayer(), this, getBuilder(), GuiType.COMPACT).open();
    }
}
