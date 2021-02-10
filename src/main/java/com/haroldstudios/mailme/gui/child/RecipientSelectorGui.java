package com.haroldstudios.mailme.gui.child;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.conversations.ConsoleMailInput;
import com.haroldstudios.mailme.conversations.LetterInputPrompt;
import com.haroldstudios.mailme.conversations.PlayerSearch;
import com.haroldstudios.mailme.gui.AbstractScrollingMailGui;
import com.haroldstudios.mailme.gui.Expandable;
import com.haroldstudios.mailme.gui.GuiOptions;
import com.haroldstudios.mailme.mail.*;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.PlayerUtils;
import me.mattstudios.gui.components.util.ItemBuilder;
import me.mattstudios.gui.components.xseries.XMaterial;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RecipientSelectorGui extends AbstractScrollingMailGui implements Expandable {

    private final List<OfflinePlayer> playerList;

    public RecipientSelectorGui(final MailMe plugin, final Mail.Builder<?> builder, final GuiOptions guiOptions) {
        super(plugin, builder, guiOptions);

        GuiItem searchPlayers = new GuiItem(getPlugin().getLocale().getItemStack(getPlayer(),"gui.search-players"), event -> {
            PlayerSearch.begin(plugin, builder, getPlayer());
            gracefulExit = true;
            getGui().close(getPlayer());
        });
        GuiItem removeFilter = new GuiItem(getFilterItem(), event -> {
            new RecipientSelectorGui(getPlugin(), getBuilder(), getGuiOptions().setPreviousMenu(this)).open();
            PlayerUtils.playUISound(getPlayer());
        });
        GuiItem nextMenu = new GuiItem(getNextMenuButton(), event -> {
            PlayerUtils.playUISound(getPlayer());
            if (getBuilder().getRecipients().size() > 0) {
                next();
            }
        });
        addItem(searchPlayers, getGuiConfig().getItemGContainer("recipient-selector-menu.search-players"), guiOptions.getGuiType());
        addItem(removeFilter, getGuiConfig().getItemGContainer("recipient-selector-menu.remove-filter"), guiOptions.getGuiType());
        addItem(nextMenu, getGuiConfig().getItemGContainer("recipient-selector-menu.next-menu"), guiOptions.getGuiType());
        addExpandableItems(this, guiOptions.getGuiType());
        this.playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public RecipientSelectorGui(final MailMe plugin, final Mail.Builder<?> builder, final GuiOptions guiOptions, final OfflinePlayer p) {
        this(plugin, builder, guiOptions);
        this.playerList.clear();
        this.playerList.add(p);
    }

    public static GuiOptions getDefaultGuiOptions(final Player player) {
        return new GuiOptions()
                .setForWhom(player)
                .withTitle(MailMe.getInstance().getLocale().getMessage("gui.titles.recipient-selector"))
                .withRows(6);
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

            PlayerUtils.playUISound(getPlayer());
            if (getBuilder().isRecipient(p.getUniqueId())) {
                getBuilder().removeRecipient(p.getUniqueId());
            } else {
                getBuilder().addRecipient(p.getUniqueId());
            }
            getGui().updatePageItem(event.getSlot(), getItemForPlayer(p));
        });
    }

    @Override
    protected void nextMenu() {

        if (getBuilder() instanceof MailMessage.Builder) {
            gracefulExit = true;
            getGui().close(getPlayer());
            LetterInputPrompt.begin(getPlugin(), getBuilder(), getPlayer(), null);
        } else if (getBuilder() instanceof MailItems.Builder) {
            new ItemInputGui(getPlugin(), getBuilder(), new ArrayList<>(), getGuiOptions().withPreviousMenu(this)).open();
        } else if (getBuilder() instanceof MailBook.Builder) {
            new ClickToSendGui(getPlugin(), getBuilder(), getGuiOptions().withPreviousMenu(this)).open();
        } else if (getBuilder() instanceof MailConsoleCommand.Builder) {
            gracefulExit = true;
            getGui().close(getPlayer());
            ConsoleMailInput.begin(getPlugin(), getBuilder(), getPlayer(), null);
        }
    }

    @Override
    public void expand() {
        new RecipientSelectorGui(getPlugin(), getBuilder(), getGuiOptions().withPreviousMenu(this).withGuiType(GuiType.EXPANDED)).open();
    }

    @Override
    public void collapse() {
        new RecipientSelectorGui(getPlugin(), getBuilder(), getGuiOptions().withPreviousMenu(this).withGuiType(GuiType.COMPACT)).open();
    }
}