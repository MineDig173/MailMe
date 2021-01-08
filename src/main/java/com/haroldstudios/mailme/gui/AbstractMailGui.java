package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import me.mattstudios.gui.guis.BaseGui;
import me.mattstudios.gui.guis.Gui;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMailGui {

    private final MailMe plugin;
    @Nullable private final AbstractMailGui previousMenu;
    private final Player player;
    private BaseGui gui;
    private Mail.Builder<?> builder;
    private Runnable runnable;

    private AbstractMailGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder) {
        this.plugin = plugin;
        this.player = player;
        this.previousMenu = previousMenu;
        this.builder = builder;
    }

    public AbstractMailGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, int rows, String name, Mail.Builder<?> builder) {
        this(plugin, player, previousMenu, new Gui(rows,name), builder);
    }

    public AbstractMailGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, BaseGui gui, Mail.Builder<?> builder) {
        this(plugin, player, previousMenu, builder);
        this.gui = gui;
        gui.setDefaultClickAction(event -> {
            if (event.getClick().equals(ClickType.SHIFT_LEFT) || event.getClick().equals(ClickType.SHIFT_RIGHT)) {
                event.setCancelled(true);

            }

            if (event.getClickedInventory() != null && !event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                event.setCancelled(true);
            }
        });
    }

    // Overrides next action. Will run this instead of next code
    public AbstractMailGui withRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    // What to do on completion of the menu. If runnable is null, it goes to next stage of mail builder.
    public void next() {
        if (runnable != null) {
            runnable.run();
            return;
        }
        nextMenu();
    }
    abstract void nextMenu();

    public abstract void open();

    public Mail.Builder<?> getBuilder() {
        return builder;
    }

    public void setBuilder(Mail.Builder<?> builder) {
        this.builder = builder;
    }

    protected MailMe getPlugin() {
        return plugin;
    }

    protected Player getPlayer() {
        return player;
    }

    public BaseGui getGui() {
        return gui;
    }

    @Nullable protected AbstractMailGui getPreviousMenu() {
        return previousMenu;
    }

    protected GuiItem getFillerItem() {
        return new GuiItem(plugin.getLocale().getItemStack(player,"gui.filler"));
    }

    protected GuiItem getPreviousMenuButton() {
        return new GuiItem(plugin.getLocale().getItemStack(player,"gui.previous-menu"), event -> {
            playUISound();
            if (previousMenu == null) return;
            previousMenu.open();
        });
    }

    protected ItemStack getFilterItem() { return getPlugin().getLocale().getItemStack(player,"gui.remove-filters"); }

    protected ItemStack getExpandableItem() { return getPlugin().getLocale().getItemStack(player, "gui.expand"); }

    protected ItemStack getCollapsableItem() { return getPlugin().getLocale().getItemStack(player, "gui.collapse"); }

    protected void playUISound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
    }

    protected GuiItem getCloseMenu() {
        return new GuiItem(plugin.getLocale().getItemStack(player,"gui.close-menu"), event -> gui.close(player));
    }

    protected GuiItem getExpandItem() {
        return new GuiItem(getPlugin().getLocale().getItemStack(player,"gui.expand"));
    }

    protected ItemStack getNextMenuButton() {
        return getPlugin().getLocale().getItemStack(player,"gui.next-menu");
    }
}