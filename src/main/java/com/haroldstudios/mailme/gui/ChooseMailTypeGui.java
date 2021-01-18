package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.*;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.GuiConfig;
import com.haroldstudios.mailme.utils.PermissionConstants;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.gui.components.util.ItemBuilder;
import me.mattstudios.gui.components.xseries.XMaterial;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ChooseMailTypeGui extends AbstractMailGui {

    private boolean anonymous = false;

    public ChooseMailTypeGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder) {
        super(plugin, player, previousMenu, 3, plugin.getLocale().getMessage(player, "gui.titles.choose-type"), builder);
        getGui().getFiller().fill(getFillerItem());

        addItem(getCloseMenu(), getGuiConfig().getItemGContainer("choose-type-menu.exit", getGui().getRows()));

        if (player.hasPermission(PermissionConstants.SEND_ANONYMOUS)) {
            addItem(getSetAnonymousItem(), getGuiConfig().getItemGContainer("choose-type-menu.anonymous", getGui().getRows()));
        }
    }

    @Override
    public void open() {

        GuiItem messageItem = new GuiItem(getPlugin().getLocale().getItemStack(getPlayer(), "gui.message"), e -> {
            if (!Utils.passedPermissionCheck(getPlayer(), PermissionConstants.SEND_MAIL_MESSAGE)) return;
            createMailBuilder(MailType.MESSAGE);
            next();
        });
        addItem(messageItem, getGuiConfig().getItemGContainer("choose-type-menu.message", getGui().getRows()));

        GuiItem itemsItem = new GuiItem(getPlugin().getLocale().getItemStack(getPlayer(), "gui.item"), e -> {
            if (!Utils.passedPermissionCheck(getPlayer(), PermissionConstants.SEND_MAIL_ITEM)) return;
            createMailBuilder(MailType.ITEM);
            next();
        });
        addItem(itemsItem, getGuiConfig().getItemGContainer("choose-type-menu.items", getGui().getRows()));
        GuiItem booksItem = new GuiItem(getPlugin().getLocale().getItemStack(getPlayer(), "gui.book"), e -> {
            if (!Utils.passedPermissionCheck(getPlayer(), PermissionConstants.SEND_MAIL_BOOK)) return;
            ItemStack cursor = e.getCursor();
            if (cursor == null || !cursor.getType().equals(XMaterial.WRITTEN_BOOK.parseMaterial()) || !cursor.hasItemMeta()) {
                getPlayer().sendMessage(getPlugin().getLocale().getMessage(getPlayer(), "requires-written-book"));
                return;
            }

            createMailBuilder(MailType.BOOK);
            MailBook.Builder builder = (MailBook.Builder) getBuilder();
            builder.setBook(new ItemStack(cursor));
            e.setCursor(null);
            next();
        });
        addItem(booksItem, getGuiConfig().getItemGContainer("choose-type-menu.books", getGui().getRows()));

        GuiItem consoleItem = new GuiItem(getPlugin().getLocale().getItemStack(getPlayer(),"gui.console"), event -> {
            if (!Utils.passedPermissionCheck(getPlayer(), PermissionConstants.SEND_CONSOLE_COMMAND)) return;
            createMailBuilder(MailType.CONSOLE_CMD);
            next();
        });

        if (getPlayer().hasPermission(PermissionConstants.SEND_CONSOLE_COMMAND)) {
            addItem(consoleItem, getGuiConfig().getItemGContainer("choose-type-menu.console"));
        }

        getGui().open(getPlayer());
    }

    @Override
    void nextMenu() {
        if (anonymous) {
            getBuilder().setSender(getPlugin().getLocale().getMessage(getPlayer(), "mail.anonymous"));
        } else {
            getBuilder().setSender(getPlayer().getName());
        }
        playUISound();
        new IconSelectorGui(getPlugin(), getPlayer(), this, getBuilder()).open();
    }

    public GuiItem getSetAnonymousItem() {
        ItemBuilder stack = ItemBuilder.from(getPlugin().getLocale().getItemStack(getPlayer(), "gui.anonymous"));
        if (getBuilder() == null) {
            createMailBuilder(MailType.MESSAGE);
        }

        if (anonymous) {
            stack.glow(true);
        }
        return stack.asGuiItem(event -> {
            anonymous = !anonymous;
            getGui().updateItem(1,5,getSetAnonymousItem());
        });

    }

    private void createMailBuilder(MailType type) {
        switch(type) {
            case ITEM:
                setBuilder(new MailItems.Builder().combine(getBuilder()));
                break;
            case BOOK:
                setBuilder(new MailBook.Builder().combine(getBuilder()));
                break;
            case MESSAGE:
                setBuilder(new MailMessage.Builder().combine(getBuilder()));
                break;
            case CONSOLE_CMD:
                setBuilder(new MailConsoleCommand.Builder().combine(getBuilder()));
                break;
        }
    }
}
