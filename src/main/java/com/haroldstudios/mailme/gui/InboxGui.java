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

public class InboxGui extends AbstractScrollingMailGui implements Expandable {

    private Mail[] mailArray;
    private final GuiType type;

    public InboxGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder, GuiType type) {
        super(plugin, player, previousMenu, 6, plugin.getLocale().getMessage(player, "gui.titles.inbox"), builder, type);
        this.type = type;

        getGui().setItem(InteractableItem.READ_AS_TEXT.getRow(type), InteractableItem.READ_AS_TEXT.getCol(type), new GuiItem(plugin.getLocale().getItemStack(player, "gui.read-as-text"), event -> {
            getGui().close(player);
            player.performCommand("mailme text");
        }));

        getGui().setItem(InteractableItem.CLOSE_MENU.getRow(type), InteractableItem.CLOSE_MENU.getCol(type), getCloseMenu());
        getGui().setItem(InteractableItem.FILTER_UNREAD.getRow(type), InteractableItem.FILTER_UNREAD.getCol(type), new GuiItem(plugin.getLocale().getItemStack(player, "gui.filter-unread"), event -> plugin.getPlayerMailDAO().getUnreadMail(player.getUniqueId()).thenAccept(mail -> Bukkit.getScheduler().runTask(getPlugin(), () -> new InboxGui(plugin,player,previousMenu,builder,type,mail).open()))));
        getGui().setItem(InteractableItem.REMOVE_FILTER.getRow(type),InteractableItem.REMOVE_FILTER.getCol(type), new GuiItem(getFilterItem(), event -> new InboxGui(plugin,player,previousMenu,builder, type).open()));
        getGui().setItem(InteractableItem.INFO.getRow(type),InteractableItem.INFO.getCol(type), new GuiItem(plugin.getLocale().getItemStack(player, "gui.inbox")));

        addExpandableItems(this, type);
    }

    public InboxGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder, GuiType type, Mail[] mail) {
        this(plugin,player,previousMenu,builder, type);
        this.mailArray = mail;
    }

    @Override
    void nextMenu() {
    }

    @Override
    public void open() {
        if (mailArray == null) {
            getPlugin().getPlayerMailDAO().getAllMail(getPlayer().getUniqueId()).thenAccept(mailArr -> {
                mailArray = mailArr;
                Bukkit.getScheduler().runTask(getPlugin(), this::initializeForPlayer);
            });
            return;
        } else {
            initializeForPlayer();
        }
    }

    private void initializeForPlayer() {
        GuiItem[] guiItems = new GuiItem[mailArray.length];
        for (int i = 0; i < mailArray.length; i++) {
            Mail mail = mailArray[i];
            ItemBuilder itemBuilder = ItemBuilder.from(Utils.getItemFromMail(mailArray[i], getPlayer()));
            itemBuilder.glow(!mail.isRead());
            GuiItem item = itemBuilder.asGuiItem();
            item.setAction(event -> {
                if (event.getClick().isLeftClick()) {
                    if (!mail.isRead()) {
                        mail.setRead(true);
                        getPlugin().getPlayerMailDAO().setUnread(getPlayer().getUniqueId(), mail).thenAccept(bool -> {
                            if (bool) {
                                mail.onMailClick(getPlayer());
                                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> new InboxGui(getPlugin(), getPlayer(), getPreviousMenu(), getBuilder(), type).open(), 1L);
                            } else {
                                System.out.println("Failed to save to Database");
                            }
                        });
                        return;
                    }
                    if (mail instanceof MailItems) return;
                    mail.onMailClick(getPlayer());
                } else {
                    getGui().updatePageItem(event.getSlot(), new GuiItem(getAreYouSure(), e -> {
                        if (e.getClick().isRightClick()) {
                            getPlugin().getPlayerMailDAO().deletePlayerMail(mail);
                            getGui().updatePageItem(e.getSlot(), ItemBuilder.from(getPlugin().getLocale().getItemStack(getPlayer(), "gui.deleted")).asGuiItem());
                        } else {
                            getGui().updatePageItem(e.getSlot(), item);
                        }
                    }));
                }
            });
            guiItems[i] = item;
        }
        getGui().addItem(guiItems);
        getGui().update();

        getGui().open(getPlayer());
    }

    private ItemStack getAreYouSure() {
        return getPlugin().getLocale().getItemStack(getPlayer(), "gui.are-you-sure");
    }

    @Override
    public void expand() {
        new InboxGui(getPlugin(), getPlayer(), getPreviousMenu(), getBuilder(), GuiType.EXPANDED, mailArray).open();
    }

    @Override
    public void collapse() {
        new InboxGui(getPlugin(), getPlayer(), getPreviousMenu(), getBuilder(), GuiType.COMPACT, mailArray).open();
    }

    private enum InteractableItem {
        READ_AS_TEXT(5,4,5,4),
        CLOSE_MENU(6,5,6,5),
        FILTER_UNREAD(5,5,5,5),
        REMOVE_FILTER(5,6,5,6),
        INFO(1,5,1,5);

        private final int compactRow, compactCol,
                    expandedRow, expandedCol;

        InteractableItem(final int compactRow, final int compactCol, final int expandedRow, final int expandedCol) {
            this.compactRow = compactRow;
            this.compactCol = compactCol;
            this.expandedRow = expandedRow;
            this.expandedCol = expandedCol;
        }

        public int getCol(GuiType type) {
            if (type == GuiType.COMPACT) {
                return compactCol;
            } else {
                return expandedCol;
            }
        }

        public int getRow(GuiType type) {
            if (type == GuiType.COMPACT) {
                return compactRow;
            } else {
                return expandedRow;
            }
        }
    }
}
