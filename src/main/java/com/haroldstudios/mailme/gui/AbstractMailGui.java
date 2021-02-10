package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.utils.GuiConfig;
import com.haroldstudios.mailme.utils.PlayerUtils;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.gui.guis.BaseGui;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;


public abstract class AbstractMailGui {

    private final MailMe plugin;
    private BaseGui gui; // This gui

    private final GuiOptions guiOptions;
    private Mail.Builder<?> builder;
    public boolean gracefulExit;
    private final GuiConfig guiConfig;

    private AbstractMailGui(final MailMe plugin, final Mail.Builder<?> builder, final GuiOptions guiOptions) {
        this.plugin = plugin;
        this.builder = builder;
        this.guiConfig = plugin.getGuiConfig();
        this.guiOptions = guiOptions;
        if (guiOptions.getPreviousMenu() != null) {
            guiOptions.getPreviousMenu().gracefulExit = true;
        }
    }

    public AbstractMailGui(final MailMe plugin, final BaseGui gui, final Mail.Builder<?> builder, final GuiOptions guiOptions) {
        this(plugin, builder, guiOptions);
        this.gui = gui;

        gui.setDefaultTopClickAction(event -> event.setCancelled(true));
        gui.setDefaultClickAction(event -> {
            if (event.getClick().equals(ClickType.SHIFT_LEFT) || event.getClick().equals(ClickType.SHIFT_RIGHT)) {
                event.setCancelled(true);
            }
            if (event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
            event.setCancelled(true);
            PlayerUtils.playUISound(getPlayer());
        });
        gui.setDragAction(event -> event.setCancelled(true));
        gui.setCloseGuiAction(event -> {
            if (gracefulExit || getBuilder() == null) return;
            for (ItemStack stack : getBuilder().getInputtedItems()) {
                Utils.giveItem(guiOptions.getForWhom(), stack);
            }
        });
    }

    protected void addItem(GuiItem item, GuiConfig.GContainer container) {
        if (container.isEnabled()) {
            getGui().setItem(container.getRow(), container.getCol(), item);
        }
    }

    protected void addItem(GuiItem item, GuiConfig.GContainer container, Expandable.GuiType type) {
        if (container.isEnabled()) {
            getGui().setItem(container.getRow(type), container.getCol(type), item);
        }
    }

    // What to do on completion of the menu. If runnable is null, it does default.
    public void next() {
        if (guiOptions.getRunnable() != null) {
            guiOptions.getRunnable().run();
        } else {
            nextMenu();
        }
    }

    public GuiOptions getGuiOptions() {
        return guiOptions;
    }

    // Saves us typing the commonly used method out
    protected Player getPlayer() {
        return guiOptions.getForWhom();
    }

    public Mail.Builder<?> getBuilder() {
        return builder;
    }
    public void setBuilder(Mail.Builder<?> builder) {
        this.builder = builder;
    }
    protected MailMe getPlugin() {
        return plugin;
    }
    protected GuiConfig getGuiConfig() {
        return guiConfig;
    }
    public BaseGui getGui() {
        return gui;
    }
    protected GuiItem getFillerItem() {
        return new GuiItem(plugin.getLocale().getItemStack(guiOptions.getForWhom(),"gui.filler"));
    }
    protected ItemStack getFilterItem() { return getPlugin().getLocale().getItemStack(guiOptions.getForWhom(),"gui.remove-filters"); }
    protected ItemStack getExpandableItem() { return getPlugin().getLocale().getItemStack(guiOptions.getForWhom(), "gui.expand"); }
    protected ItemStack getCollapsableItem() { return getPlugin().getLocale().getItemStack(guiOptions.getForWhom(), "gui.collapse"); }
    protected GuiItem getCloseMenu() {
        return new GuiItem(plugin.getLocale().getItemStack(guiOptions.getForWhom(),"gui.close-menu"), event -> gui.close(guiOptions.getForWhom()));
    }
    protected ItemStack getNextMenuButton() {
        return getPlugin().getLocale().getItemStack(guiOptions.getForWhom(),"gui.next-menu");
    }

    protected abstract void nextMenu();
    public abstract void open();
}