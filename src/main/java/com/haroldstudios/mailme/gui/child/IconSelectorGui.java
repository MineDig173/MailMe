package com.haroldstudios.mailme.gui.child;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.gui.AbstractScrollingMailGui;
import com.haroldstudios.mailme.gui.GuiOptions;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class IconSelectorGui extends AbstractScrollingMailGui {

    public IconSelectorGui(final MailMe plugin, final Mail.Builder<?> builder, final GuiOptions guiOptions) {
        super(plugin, builder, guiOptions);
        addItem(getCloseMenu(), getGuiConfig().getItemGContainer("icon-selector-menu.exit"));
    }

    public static GuiOptions getDefaultGuiOptions(Player player) {
        return new GuiOptions()
                .withRows(5)
                .setForWhom(player)
                .withTitle(MailMe.getInstance().getLocale().getMessage("gui.titles.icon-selector"))
                .withGuiType(MailMe.getInstance().getGuiConfig().getGuiTypeFor("recipient-selector-menu"));
    }

    @Override
    protected void nextMenu() {
        new RecipientSelectorGui(getPlugin(), getBuilder(), RecipientSelectorGui.getDefaultGuiOptions(getPlayer()).withPreviousMenu(this)).open();
    }

    @Override
    public void open() {
        ConfigValue.ICON_LIST.forEach(icon -> getItems().add(new GuiItem(icon, event -> {
            getBuilder().setIcon(icon);
            next();
        })));


        getGui().addItem(getItems().toArray(new GuiItem[0]));

        ItemStack stack = getPlugin().getLocale().getItemStack(getPlayer(), "gui.icon-custom");

        GuiItem customIcon = new GuiItem(stack, event -> {
            if (event.getCursor() == null) return;
            if (event.getCursor().getType().equals(Material.AIR)) return;

            ItemStack cursor = event.getCursor();

            getBuilder().setIcon(new org.bukkit.inventory.ItemStack(event.getCursor()));
            event.setCursor(null);

            Utils.giveItem(getPlayer(), cursor);
            next();
        });
        addItem(customIcon, getGuiConfig().getItemGContainer("icon-selector-menu.exit"));

        getGui().open(getPlayer());
    }
}