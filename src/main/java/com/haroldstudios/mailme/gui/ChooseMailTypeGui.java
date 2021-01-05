package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.*;
import me.mattstudios.mfgui.gui.components.xseries.XMaterial;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ChooseMailTypeGui extends AbstractMailGui {

    public ChooseMailTypeGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder) {
        super(plugin, player, previousMenu,3,plugin.getLocale().getMessage(player, "gui.titles.choose-type"), builder);
        getGui().getFiller().fill(getFillerItem());
        getGui().setItem(3,5,getCloseMenu());
    }

    @Override
    public void open() {

        getGui().setItem(2,4, new GuiItem(getPlugin().getLocale().getItemStack("gui.message"), e -> {
            createMailBuilder(MailType.MESSAGE);
            next();
        }));
        getGui().setItem(2,5, new GuiItem(getPlugin().getLocale().getItemStack("gui.item"), e -> {
            createMailBuilder(MailType.ITEM);
            next();
        }));
        getGui().setItem(2,6, new GuiItem(getPlugin().getLocale().getItemStack("gui.book"), e -> {
            ItemStack cursor = e.getCursor();
            if (cursor == null || !cursor.getType().equals(XMaterial.WRITTEN_BOOK.parseMaterial().get()) || !cursor.hasItemMeta()) {
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
        playUISound();
        new IconSelectorGui(getPlugin(), getPlayer(), this, getBuilder().setSender(getPlayer().getName())).open(); //todo anonymous opt
    }

    private void createMailBuilder(MailType type) {
        switch(type) {
            case ITEM:
                setBuilder(new MailItems.Builder().combine(getBuilder()));
                break;
            case MESSAGE:
                setBuilder(new MailMessage.Builder().combine(getBuilder()));
                break;
            case BOOK:
                setBuilder(new MailBook.Builder().combine(getBuilder()));
                break;
            default:
        }
    }
}
