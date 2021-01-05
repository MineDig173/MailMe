package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;


public class IconSelectorGui extends AbstractScrollingMailGui{

    public IconSelectorGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder) {
        super(plugin, player, previousMenu, 6, plugin.getLocale().getMessage(player, "gui.titles.icon-selector"), builder);
        getGui().setItem(6,5,getCloseMenu());
        getGui().getFiller().fillBorder(getFillerItem());
        getGui().setItem(6,1,getPreviousMenuButton());

        // Previous item
        getGui().setItem(6, 3, getNextPage());
        // Next item
        getGui().setItem(6, 7, new GuiItem(plugin.getLocale().getItemStack(player,"gui.next-page"), event -> getGui().next()));


        getGui().getFiller().fillBetweenPoints(3,2,5,8, getFillerItem());
    }

    @Override
    void nextMenu() {
        playUISound();
        new RecipientSelectorGui(getPlugin(), getPlayer(), this, getBuilder()).open();
    }

    @Override
    public void open() {
        ConfigValue.ICON_LIST.forEach(icon -> getItems().add(new GuiItem(icon, event -> {
            getBuilder().setIcon(icon);
            next();
        })));


        getGui().addItem(getItems().toArray(new GuiItem[0]));

        ItemStack stack = getPlugin().getLocale().getItemStack(getPlayer(), "gui.icon-custom");

        getGui().setItem(3,5, new GuiItem(stack, event -> {
            if (event.getCursor() == null) return;
            if (event.getCursor().getType().equals(Material.AIR)) return;

            ItemStack cursor = event.getCursor();

            getBuilder().setIcon(new org.bukkit.inventory.ItemStack(event.getCursor()));
            event.setCursor(null);

            Utils.giveItem(getPlayer(), cursor);
            next();
        }));

        getGui().open(getPlayer());
    }
}
