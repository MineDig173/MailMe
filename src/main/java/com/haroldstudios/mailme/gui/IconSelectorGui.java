package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;


public class IconSelectorGui extends AbstractScrollingMailGui {

    public IconSelectorGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder) {
        super(plugin, player, previousMenu, 6, plugin.getLocale().getMessage(player, "gui.titles.icon-selector"), builder, Expandable.GuiType.COMPACT);
        getGui().setItem(6,5,getCloseMenu());
    }

    @Override
    void nextMenu() {
        playUISound();
        new RecipientSelectorGui(getPlugin(), getPlayer(), this, getBuilder(), Expandable.GuiType.COMPACT).open();
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
