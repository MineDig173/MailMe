package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.*;
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
        super(plugin, player, previousMenu,3,plugin.getLocale().getMessage(player, "gui.titles.choose-type"), builder);
        getGui().getFiller().fill(getFillerItem());
        getGui().setItem(3,5,getCloseMenu());
        if (player.hasPermission(PermissionConstants.SEND_ANONYMOUS)) {
            getGui().setItem(1, 5, getSetAnonymousItem());
        }
    }

    @Override
    public void open() {

        getGui().setItem(2,4, new GuiItem(getPlugin().getLocale().getItemStack(getPlayer(),"gui.message"), e -> {
            if (!Utils.passedPermissionCheck(getPlayer(), PermissionConstants.SEND_MAIL_MESSAGE)) return;
            createMailBuilder(MailType.MESSAGE);
            next();
        }));
        getGui().setItem(2,5, new GuiItem(getPlugin().getLocale().getItemStack(getPlayer(),"gui.item"), e -> {
            if (!Utils.passedPermissionCheck(getPlayer(), PermissionConstants.SEND_MAIL_ITEM)) return;
            createMailBuilder(MailType.ITEM);
            next();
        }));
        getGui().setItem(2,6, new GuiItem(getPlugin().getLocale().getItemStack(getPlayer(),"gui.book"), e -> {
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
        }));

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
            if (anonymous) {
                anonymous = false;
            } else {
                anonymous = true;
            }
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
        }
    }
}
