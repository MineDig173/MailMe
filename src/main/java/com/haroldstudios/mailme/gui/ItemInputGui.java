package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailItems;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemInputGui extends AbstractMailGui {

    private final List<ItemStack> items;

    public ItemInputGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder, List<ItemStack> items) {
        super(plugin, player, previousMenu, 6, plugin.getLocale().getMessage(player, "gui.titles.item-input"), builder);
        this.items = items;

        getGui().setCloseGuiAction(event -> {
            if (gracefulExit) return;
            getInputtedItems(event.getInventory()).forEach(item -> Utils.giveItem(player, item));
        });

        getGui().setDefaultTopClickAction(event -> {
            if (isValidItemSlot(event.getSlot())) {
                return;
            }
            event.setCancelled(true);
        });
        getGui().setDefaultClickAction(event -> { });
        getGui().setDragAction(event -> { });
        GuiItem title = new GuiItem(plugin.getLocale().getItemStack(player, "gui.item-input-title"));
        GuiItem nextMenu = new GuiItem(getNextMenuButton(), event -> {
            items.addAll(getInputtedItems(event.getInventory()));

            if (items.isEmpty()) return;
            next();
        });

        getGui().getFiller().fillBorder(getFillerItem());
        getGui().getFiller().fillBetweenPoints(2,8,5,8, getFillerItem());
        getGui().getFiller().fillBetweenPoints(5,2,5,8, getFillerItem());
        getGui().getFiller().fillBetweenPoints(2,2,5,2, getFillerItem());

        addItem(getCloseMenu(), getGuiConfig().getItemGContainer("item-input-menu.exit"));
        addItem(title, getGuiConfig().getItemGContainer("item-input-menu.title"));
        addItem(nextMenu, getGuiConfig().getItemGContainer("item-input-menu.next-menu"));

    }

    private boolean isValidItemSlot(int slot) {
        return (slot >= 11 && slot <= 15) || (slot >= 20 && slot <= 24) || (slot >= 29 && slot <= 33);
    }

    public List<ItemStack> getInputtedItems(Inventory inventory) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 10; i <= 33; i++) {
            if (!isValidItemSlot(i)) continue;
            ItemStack item = inventory.getItem(i);
            if (item == null)
                continue;
            org.bukkit.inventory.ItemStack stack = new ItemStack(item); // Convert from NMS ItemStack to Bukkit ItemStack
            list.add(stack);
        }
        return list;
    }

    @Override
    void nextMenu() {
        gracefulExit = true;
        MailItems.Builder mb = (MailItems.Builder) getBuilder();
        // Ensures it's not an NMS item as we can't / don't serialize those.
        mb.setItemStackList(items.stream().map(ItemStack::new).collect(Collectors.toList()));
        new ClickToSendGui(getPlugin(), getPlayer(), this, getBuilder()).open();
    }

    @Override
    public void open() {
        getGui().open(getPlayer());
    }
}