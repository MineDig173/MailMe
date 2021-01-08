package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import me.mattstudios.gui.components.ScrollType;
import me.mattstudios.gui.guis.GuiItem;
import me.mattstudios.gui.guis.ScrollingGui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScrollingMailGui extends AbstractMailGui {

    private final ScrollingGui gui;
    private final List<GuiItem> items = new ArrayList<>();

    public AbstractScrollingMailGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, int rows, String name, Mail.Builder<?> builder, Expandable.GuiType type) {
        super(plugin, player, previousMenu, new ScrollingGui(rows, type.getPageSize(), name, type == Expandable.GuiType.COMPACT ? ScrollType.HORIZONTAL : ScrollType.VERTICAL), builder);

        gui = (ScrollingGui) super.getGui();

        gui.getFiller().fillBetweenPoints(InteractableItem.FILL_FROM.getRow(type),InteractableItem.FILL_FROM.getCol(type),InteractableItem.FILL_TO.getRow(type),InteractableItem.FILL_TO.getCol(type), getFillerItem());
        getGui().getFiller().fillBorder(getFillerItem());

        // Previous item
        gui.setItem(InteractableItem.PREVIOUS_PAGE.getRow(type), InteractableItem.PREVIOUS_PAGE.getCol(type), getPreviousPage());
        // Next item
        gui.setItem(InteractableItem.NEXT_PAGE.getRow(type), InteractableItem.NEXT_PAGE.getCol(type), getNextPage());
    }

    protected GuiItem getPreviousPage() {
        return new GuiItem(getPlugin().getLocale().getItemStack(getPlayer(),"gui.previous-page"), event -> getGui().previous());
    }

    protected GuiItem getNextPage() {
        return new GuiItem(getPlugin().getLocale().getItemStack(getPlayer(), "gui.next-page"), event -> getGui().next());
    }

    public List<GuiItem> getItems() {
        return items;
    }

    @Override
    public ScrollingGui getGui() {
        return gui;
    }

    public void addExpandableItems(Expandable expandable, Expandable.GuiType type) {
        if (type == Expandable.GuiType.COMPACT) {
            getGui().setItem(InteractableItem.EXPANDABLE.getRow(type), InteractableItem.EXPANDABLE.getCol(type), new GuiItem(getExpandableItem(), event -> expandable.expand()));
        } else {
            getGui().getFiller().fillBetweenPoints(2,8,4,8, getFillerItem());
            getGui().setItem(InteractableItem.COLLAPSABLE.getRow(type), InteractableItem.COLLAPSABLE.getCol(type), new GuiItem(getCollapsableItem(), event -> expandable.collapse()));
        }
    }

    private enum InteractableItem {
        PREVIOUS_PAGE(2,1,2,9),
        NEXT_PAGE(2,9,4,9),
        FILL_FROM(3,2,5,2),
        FILL_TO(5,8,5,8),
        EXPANDABLE(4,5,0,0),
        COLLAPSABLE(0,0,3,9);

        private final int compactRow,compactCol,
                           expandedRow,expandedCol;

        InteractableItem(final int compactRow, final int compactCol, final int expandedRow, final int expandedCol) {
            this.compactCol = compactCol;
            this.compactRow = compactRow;
            this.expandedCol = expandedCol;
            this.expandedRow = expandedRow;
        }

        public int getRow(Expandable.GuiType type) {
            if (type == Expandable.GuiType.COMPACT) {
                return compactRow;
            } else {
                return expandedRow;
            }
        }

        public int getCol(Expandable.GuiType type) {
            if (type == Expandable.GuiType.COMPACT) {
                return compactCol;
            } else {
                return expandedCol;
            }
        }
    }
}
