package com.haroldstudios.mailme.gui.child;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.gui.AbstractMailGui;
import com.haroldstudios.mailme.gui.GuiOptions;
import com.haroldstudios.mailme.mail.Mail;
import me.mattstudios.gui.guis.Gui;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class FilterGui extends AbstractMailGui {

    // Filters
    // Read All, Delete All Currently Selected (and a confirm button), Archived (needs impl), read all unread, all read,

    public FilterGui(final MailMe plugin, final Mail.Builder<?> builder, final GuiOptions guiOptions) {
        super(plugin, new Gui(guiOptions.getRows(), guiOptions.getTitle()), builder, guiOptions);

        getGui().getFiller().fill(getFillerItem());

        GuiItem unreadMail = new GuiItem(plugin.getLocale().getItemStack("gui.filters.unread"), event -> plugin.getPlayerMailDAO().getUnreadMail(guiOptions.getForWhom().getUniqueId()).thenApply(mail -> Bukkit.getScheduler().runTask(plugin,() -> new InboxGui(plugin, builder, InboxGui.getDefaultGuiOptions(getPlayer()).withPreviousMenu(guiOptions.getPreviousMenu()).withMail(mail)).open())));
        GuiItem deleteAllSelectedMail = new GuiItem(plugin.getLocale().getItemStack("gui.filters.delete-all"));
        GuiItem deleteAllSelectedMailConfirm = new GuiItem(plugin.getLocale().getItemStack("gui.filters.delete-all-check"), event -> {
            if (event.getClick().isLeftClick()) {
                plugin.getPlayerMailDAO().deletePlayerMail(getPlayer().getUniqueId(), guiOptions.getMailList().stream().filter(mail -> !mail.isArchived()).toArray(Mail[]::new)).thenRun(() -> new InboxGui(plugin, builder, InboxGui.getDefaultGuiOptions(getPlayer())).open());
            } else if (event.getClick().isRightClick()) {
                getGui().updateItem(event.getSlot(), deleteAllSelectedMail);
            }
        });
        deleteAllSelectedMail.setAction(event -> getGui().updateItem(event.getSlot(), deleteAllSelectedMailConfirm));

        GuiItem archivedMail = new GuiItem(plugin.getLocale().getItemStack("gui.filters.archived"), event -> plugin.getPlayerMailDAO().getAllMail(getPlayer().getUniqueId()).thenApply(mail -> Bukkit.getScheduler().runTask(plugin, () -> new InboxGui(plugin, builder, InboxGui.getDefaultGuiOptions(getPlayer()).withPreviousMenu(guiOptions.getPreviousMenu()).withMail((Arrays.stream(mail).filter(Mail::isArchived).toArray(Mail[]::new)))).open())));
        GuiItem readMail = new GuiItem(plugin.getLocale().getItemStack("gui.filters.read"), event -> plugin.getPlayerMailDAO().getAllMail(getPlayer().getUniqueId()).thenApply(mail -> Bukkit.getScheduler().runTask(plugin, () -> new InboxGui(plugin, builder, InboxGui.getDefaultGuiOptions(getPlayer()).withPreviousMenu(guiOptions.getPreviousMenu()).withMail((Arrays.stream(mail).filter(Mail::isRead).toArray(Mail[]::new)))).open())));
        GuiItem goBack = new GuiItem(plugin.getLocale().getItemStack("gui.filters.go-back"), event -> new InboxGui(plugin, builder, InboxGui.getDefaultGuiOptions(getPlayer()).withPreviousMenu(guiOptions.getPreviousMenu()).withMail(guiOptions.getMailList().toArray(new Mail[0]))).open());

        addItem(unreadMail, getGuiConfig().getItemGContainer("filter-menu.unread"));
        addItem(deleteAllSelectedMail, getGuiConfig().getItemGContainer("filter-menu.delete-all"));
        addItem(readMail, getGuiConfig().getItemGContainer("filter-menu.read"));
        addItem(archivedMail, getGuiConfig().getItemGContainer("filter-menu.archived"));
        addItem(goBack, getGuiConfig().getItemGContainer("filter-menu.go-back"));
    }

    public static GuiOptions getDefaultGuiOptions(final Player player) {
        return new GuiOptions().withRows(2).setForWhom(player).withTitle(MailMe.getInstance().getLocale().getMessage("gui.titles.filters"));
    }

    @Override
    protected void nextMenu() {}

    @Override
    public void open() {
        getGui().open(getPlayer());
    }
}