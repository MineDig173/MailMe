package com.haroldstudios.mailme.gui.child;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.gui.AbstractScrollingMailGui;
import com.haroldstudios.mailme.gui.Expandable;
import com.haroldstudios.mailme.gui.GuiOptions;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailConsoleCommand;
import com.haroldstudios.mailme.mail.MailItems;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.gui.components.util.ItemBuilder;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

public class InboxGui extends AbstractScrollingMailGui implements Expandable {

    public InboxGui(final MailMe plugin, final Mail.Builder<?> builder, final GuiOptions guiOptions) {
        super(plugin, builder, guiOptions);

        GuiItem composeMail = new GuiItem(plugin.getLocale().getItemStack(getPlayer(), "gui.send-from-inbox"), event -> MailMe.getInstance().getMailCommandHandler().compose(getPlayer()));
        GuiItem readAsText = new GuiItem(plugin.getLocale().getItemStack(getPlayer(), "gui.read-as-text"), event -> {
            gracefulExit = true;
            getGui().close(getPlayer());
            getPlayer().performCommand("mailme text");
        });
        GuiItem filterUnread = new GuiItem(plugin.getLocale().getItemStack(getPlayer(), "gui.filters.choose-filters"), event -> new FilterGui(plugin, builder, FilterGui.getDefaultGuiOptions(getPlayer()).withPreviousMenu(this).withMail(guiOptions.getMailList().toArray(new Mail[0]))).open());
        GuiItem removeFilter = new GuiItem(getFilterItem(), event -> new InboxGui(plugin, builder, getDefaultGuiOptions(getPlayer())).open());
        GuiItem infoItem = new GuiItem(plugin.getLocale().getItemStack(getPlayer(), "gui.inbox"));

        addItem(readAsText, getGuiConfig().getItemGContainer("inbox-menu.read-as-text"), guiOptions.getGuiType());
        addItem(composeMail, getGuiConfig().getItemGContainer("inbox-menu.send-mail"), guiOptions.getGuiType());
        addItem(getCloseMenu(), getGuiConfig().getItemGContainer("inbox-menu.exit"), guiOptions.getGuiType());
        addItem(filterUnread, getGuiConfig().getItemGContainer("inbox-menu.filter"), guiOptions.getGuiType());
        addItem(removeFilter, getGuiConfig().getItemGContainer("inbox-menu.remove-filter"), guiOptions.getGuiType());
        addItem(infoItem, getGuiConfig().getItemGContainer("inbox-menu.info"), guiOptions.getGuiType());

        addExpandableItems(this, guiOptions.getGuiType());
    }

    @Override
    protected void nextMenu() {}

    public static GuiOptions getDefaultGuiOptions(final Player player) {
        return new GuiOptions()
                .withRows(6)
                .setForWhom(player)
                .withTitle(MailMe.getInstance().getLocale().getMessage("gui.titles.inbox"))
                .withGuiType(MailMe.getInstance().getGuiConfig().getGuiTypeFor("inbox-menu"));
    }

    @Override
    public void open() {
        if (getGuiOptions().getMailList() == null) {
            getPlugin().getPlayerMailDAO().getAllMail(getPlayer().getUniqueId()).thenAccept(mailArr -> {
                getGuiOptions().withMail(mailArr);
                Bukkit.getScheduler().runTask(getPlugin(), this::initializeForPlayer);
            });
        } else {
            initializeForPlayer();
        }
    }

    private void initializeForPlayer() {
        LinkedList<Mail> mailListRef = getGuiOptions().getMailList();
        if (mailListRef != null) {
            MailMe.debug(InboxGui.class, "MailListRef " + mailListRef.size());
            GuiItem[] guiItems = new GuiItem[mailListRef.size()];
            for (int i = 0; i < mailListRef.size(); i++) {
                guiItems[i] = getGuiItemFromMail(mailListRef.get(i));
            }
            getGui().addItem(guiItems);
            getGui().update();
        }

        getGui().open(getPlayer());
    }

    private ItemStack getAreYouSure() {
        return getPlugin().getLocale().getItemStack(getPlayer(), "gui.are-you-sure");
    }

    private GuiItem getGuiItemFromMail(Mail mail) {
        // Literal donkey code, probably should refactor in near future
        ItemBuilder itemBuilder = ItemBuilder.from(Utils.getItemFromMail(mail, getPlayer()));
        itemBuilder.glow(!mail.isRead());
        GuiItem item = itemBuilder.asGuiItem();
        item.setAction(event -> {
            if (getGuiOptions().isReadOnly()) {
                getPlayer().sendMessage(getPlugin().getLocale().getMessage(getPlayer(), "cmd.read-only"));
                return;
            }
            if (event.isShiftClick()) {
                mail.setArchived(!mail.isArchived());
                getPlugin().getPlayerMailDAO().setArchived(getPlayer().getUniqueId(), mail);
                getGui().updatePageItem(event.getSlot(), getGuiItemFromMail(mail));
            } else if (event.getClick().isLeftClick()) {
                if (!mail.isRead()) {
                    if (mail instanceof MailItems) {
                        if (!Utils.hasSpaceInInventory(((MailItems) mail).getItemStackList(), getPlayer().getInventory())) {
                            getPlayer().sendMessage(getPlugin().getLocale().getMessage(getPlayer(), "cmd.no-space"));
                            return;
                        }
                    }
                    mail.setRead(true);
                    getPlugin().getPlayerMailDAO().setRead(getPlayer().getUniqueId(), mail).thenAccept(bool -> {
                        if (bool) {
                            mail.onMailClick(getPlayer());
                            getGui().updatePageItem(event.getSlot(), getGuiItemFromMail(mail));
                        } else {
                            MailMe.debug(InboxGui.class, "Failed to save to Database");
                        }
                    });
                    return;
                }
                if (mail instanceof MailItems || mail instanceof MailConsoleCommand) return;
                mail.onMailClick(getPlayer());
            } else {
                getGui().updatePageItem(event.getSlot(), new GuiItem(getAreYouSure(), e -> {
                    if (e.getClick().isRightClick()) {
                        getPlugin().getPlayerMailDAO().deletePlayerMail(getPlayer().getUniqueId(), mail);
                        getGuiOptions().getMailList().remove(mail);
                        getGui().updatePageItem(e.getSlot(), ItemBuilder.from(getPlugin().getLocale().getItemStack(getPlayer(), "gui.deleted")).asGuiItem());
                    } else {
                        getGui().updatePageItem(e.getSlot(), item);
                    }
                }));
            }
        });
        return item;
    }

    @Override
    public void expand() {
        new InboxGui(getPlugin(), getBuilder(), getGuiOptions().withGuiType(GuiType.EXPANDED).withPreviousMenu(this)).open();
    }

    @Override
    public void collapse() {
        new InboxGui(getPlugin(), getBuilder(), getGuiOptions().withGuiType(GuiType.COMPACT).withPreviousMenu(this)).open();
    }
}