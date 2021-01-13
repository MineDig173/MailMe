package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailItems;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.gui.components.util.ItemBuilder;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;

public class InboxGui extends AbstractScrollingMailGui implements Expandable {

    private LinkedList<Mail> mailList;

    public InboxGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder, GuiType type) {
        super(plugin, player, previousMenu, 6, plugin.getLocale().getMessage(player, "gui.titles.inbox"), builder, type);

        GuiItem composeMail = new GuiItem(plugin.getLocale().getItemStack(player, "gui.send-from-inbox"), event -> {
            player.chat("/mailme compose");
        });

        GuiItem readAsText = new GuiItem(plugin.getLocale().getItemStack(player, "gui.read-as-text"), event -> {
            gracefulExit = true;
            getGui().close(player);
            player.performCommand("mailme text");
        });
        GuiItem filterUnread = new GuiItem(plugin.getLocale().getItemStack(player, "gui.filter-unread"), event -> plugin.getPlayerMailDAO().getUnreadMail(player.getUniqueId()).thenAccept(mail -> Bukkit.getScheduler().runTask(getPlugin(), () -> new InboxGui(plugin, player, this, builder, type, new LinkedList<>(Arrays.asList(mail))).open())));
        GuiItem removeFilter = new GuiItem(getFilterItem(), event -> new InboxGui(plugin, player, this, builder, type).open());
        GuiItem infoItem = new GuiItem(plugin.getLocale().getItemStack(player, "gui.inbox"));

        addItem(readAsText, getGuiConfig().getItemGContainer("inbox-menu.read-as-text"), type);
        addItem(composeMail, getGuiConfig().getItemGContainer("inbox-menu.send-mail"), type);
        addItem(getCloseMenu(), getGuiConfig().getItemGContainer("inbox-menu.exit"), type);
        addItem(filterUnread, getGuiConfig().getItemGContainer("inbox-menu.filter-unread"), type);
        addItem(removeFilter, getGuiConfig().getItemGContainer("inbox-menu.remove-filter"), type);
        addItem(infoItem, getGuiConfig().getItemGContainer("inbox-menu.info"), type);

        addExpandableItems(this, type);
    }

    public InboxGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder, GuiType type, LinkedList<Mail> mail) {
        this(plugin,player,previousMenu,builder, type);
        mailList = mail;
    }

    @Override
    void nextMenu() {
    }

    @Override
    public void open() {
        if (mailList == null) {
            getPlugin().getPlayerMailDAO().getAllMail(getPlayer().getUniqueId()).thenAccept(mailArr -> {
                mailList = new LinkedList<>();
                mailList.addAll(Arrays.asList(mailArr));
                Bukkit.getScheduler().runTask(getPlugin(), this::initializeForPlayer);
            });
        } else {
            initializeForPlayer();
        }
    }

    private void initializeForPlayer() {
        GuiItem[] guiItems = new GuiItem[mailList.size()];
        for (int i = 0; i < mailList.size(); i++) {
            guiItems[i] = getGuiItemFromMail(mailList.get(i));
        }
        getGui().addItem(guiItems);
        getGui().update();

        getGui().open(getPlayer());
    }

    private ItemStack getAreYouSure() {
        return getPlugin().getLocale().getItemStack(getPlayer(), "gui.are-you-sure");
    }

    private GuiItem getGuiItemFromMail(Mail mail) {
        ItemBuilder itemBuilder = ItemBuilder.from(Utils.getItemFromMail(mail, getPlayer()));
        itemBuilder.glow(!mail.isRead());
        GuiItem item = itemBuilder.asGuiItem();
        item.setAction(event -> {
            if (event.getClick().isLeftClick()) {
                if (!mail.isRead()) {
                    if (mail instanceof MailItems) {
                        if (!Utils.hasSpaceInInventory(((MailItems) mail).getItemStackList(), getPlayer().getInventory())) {
                            getPlayer().sendMessage(getPlugin().getLocale().getMessage(getPlayer(), "cmd.no-space"));
                            return;
                        }
                    }
                    mail.setRead(true);
                    getPlugin().getPlayerMailDAO().setUnread(getPlayer().getUniqueId(), mail).thenAccept(bool -> {
                        if (bool) {
                            mail.onMailClick(getPlayer());
                            getGui().updatePageItem(event.getSlot(), getGuiItemFromMail(mail));
                        } else {
                            MailMe.debug(InboxGui.class, "Failed to save to Database");
                        }
                    });
                    return;
                }
                if (mail instanceof MailItems) return;
                mail.onMailClick(getPlayer());
            } else {
                getGui().updatePageItem(event.getSlot(), new GuiItem(getAreYouSure(), e -> {
                    if (e.getClick().isRightClick()) {
                        getPlugin().getPlayerMailDAO().deletePlayerMail(getPlayer().getUniqueId(), mail);
                        mailList.remove(mail);
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
        new InboxGui(getPlugin(), getPlayer(), this, getBuilder(), GuiType.EXPANDED, mailList).open();
    }

    @Override
    public void collapse() {
        new InboxGui(getPlugin(), getPlayer(), this, getBuilder(), GuiType.COMPACT, mailList).open();
    }
}
