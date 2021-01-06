package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import me.mattstudios.mfgui.gui.guis.ScrollingGui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScrollingMailGui extends AbstractMailGui {

    private final ScrollingGui gui;
    private final List<GuiItem> items = new ArrayList<>();

    public AbstractScrollingMailGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, int rows, String name, Mail.Builder<?> builder) {
        super(plugin, player, previousMenu, new ScrollingGui(rows, 7, name), builder);

        gui = (ScrollingGui) super.getGui();

        gui.getFiller().fillBetweenPoints(3,2,5,8, getFillerItem());
        getGui().getFiller().fillBorder(getFillerItem());

        // Previous item
        gui.setItem(2, 1, getPreviousPage());
        // Next item
        gui.setItem(2, 9, getNextPage());

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
}
