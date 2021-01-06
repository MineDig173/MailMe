package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailItems;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemInputGui extends AbstractScrollingMailGui {

    private final List<ItemStack> items;
    // When false, items are given back on menu closure
    private boolean safeToLeave = false;

    public ItemInputGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder, List<ItemStack> items) {
        super(plugin, player, previousMenu, 6, plugin.getLocale().getMessage(player, "gui.titles.item-input"), builder);
        this.items = items;

        getGui().setItem(3, 5, new GuiItem(plugin.getLocale().getItemStack(player, "gui.item-input"), event -> {
            ItemStack cursor = event.getCursor();

            if (cursor == null) return;
            if (cursor.getType().equals(Material.AIR)) return;

            items.add(new ItemStack(cursor));
            event.setCursor(null);
            safeToLeave = true;
            new ItemInputGui(getPlugin(), getPlayer(), previousMenu, builder, items).open();
        }));

        // On close it gives player the items back
        getGui().setCloseGuiAction(event -> {
            if (!safeToLeave) {
                items.forEach(item -> Utils.giveItem(player, item));
            }
        });

        getGui().setItem(6,9, new GuiItem(getNextMenuButton(), event -> next()));
    }

    @Override
    void nextMenu() {
        safeToLeave = true;
        MailItems.Builder mb = (MailItems.Builder) getBuilder();
        // Ensures it's not an NMS item as we can't / don't serialize those.
        mb.setItemStackList(items.stream().map(ItemStack::new).collect(Collectors.toList()));
        new ClickToSendGui(getPlugin(), getPlayer(), this, getBuilder()).open();
    }

    @Override
    public void open() {

        for (ItemStack stack : items) {
            getGui().addItem(new GuiItem(stack, event -> {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null) return;

                Optional<ItemStack> itemStackOptional = items.stream().filter(item -> item.isSimilar(stack)).findFirst();
                if (itemStackOptional.isPresent()) {
                    if (items.remove(itemStackOptional.get())) {
                        Utils.giveItem(getPlayer(), itemStackOptional.get());
                    }
                }

                safeToLeave = true;
                new ItemInputGui(getPlugin(), getPlayer(), getPreviousMenu(), getBuilder(), items).open();
            }));
        }

        getGui().open(getPlayer());
    }
}
