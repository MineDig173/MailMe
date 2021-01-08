package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.conversations.LetterInputPrompt;
import com.haroldstudios.mailme.conversations.PlayerSearch;
import com.haroldstudios.mailme.mail.*;
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

        getGui().setItem(InteractableItem.SEARCH_PLAYERS.getRow(type),InteractableItem.SEARCH_PLAYERS.getCol(type), new GuiItem(getPlugin().getLocale().getItemStack(player,"gui.search-players"), event -> {
            PlayerSearch.begin(plugin, builder, player);
            getGui().close(getPlayer());
        }));
        getGui().setItem(InteractableItem.REMOVE_FILTER.getRow(type),InteractableItem.REMOVE_FILTER.getCol(type), new GuiItem(getFilterItem(), event -> {
            new RecipientSelectorGui(getPlugin(), getPlayer(), getPreviousMenu(), getBuilder(), type).open();
            playUISound();
        }));
        getGui().setItem(InteractableItem.NEXT_MENU.getRow(type),InteractableItem.NEXT_MENU.getCol(type), new GuiItem(getNextMenuButton(), event -> {
            playUISound();
            if (getBuilder().getRecipients().size() > 0) {
                next();
            }
        }));
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
            LetterInputPrompt.begin(getPlugin(), getBuilder(), getPlayer(), null);
        } else if (getBuilder() instanceof MailItems.Builder) {
            new ItemInputGui(getPlugin(), getPlayer(), this, getBuilder(), new ArrayList<>()).open();
        } else if (getBuilder() instanceof MailBook.Builder) {
            new ClickToSendGui(getPlugin(), getPlayer(), this, getBuilder()).open();
        }
    }

    @Override
    public void expand() {
        new RecipientSelectorGui(getPlugin(), getPlayer(), getPreviousMenu(), getBuilder(), GuiType.EXPANDED).open();
    }

    @Override
    public void collapse() {
        new RecipientSelectorGui(getPlugin(), getPlayer(), getPreviousMenu(), getBuilder(), GuiType.COMPACT).open();
    }

    private enum InteractableItem {
        SEARCH_PLAYERS(4,4,5,4),
        REMOVE_FILTER(4,6,5,6),
        NEXT_MENU(6,9,6,9);

        private final int compactCol, compactRow,
                          expandedCol, expandedRow;

        InteractableItem(final int compactRow, final int compactCol, final int expandedRow, final int expandedCol) {
            this.compactRow = compactRow;
            this.compactCol = compactCol;
            this.expandedRow = expandedRow;
            this.expandedCol = expandedCol;
        }

        public int getRow(GuiType type) {
            if (type == GuiType.COMPACT) {
                return compactRow;
            } else {
                return expandedRow;
            }
        }

        public int getCol(GuiType type) {
            if (type == GuiType.COMPACT) {
                return compactCol;
            } else {
                return expandedCol;
            }
        }
    }
}
