package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class InboxGui extends AbstractScrollingMailGui {

    public InboxGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder) {
        super(plugin, player, previousMenu, 6, plugin.getLocale().getMessage(player, "gui.titles.inbox"), builder);
    }

    @Override
    void nextMenu() { }

    @Override
    public void open() {
        getPlugin().getPlayerMailDAO().getUnreadMail(getPlayer().getUniqueId()).thenAccept(mailArray -> {
            GuiItem[] guiItems = new GuiItem[mailArray.length];
            for (int i = 0; i < mailArray.length; i++) {
                int finalI = i;
                guiItems[i] = new GuiItem(Utils.getItemFromMail(mailArray[i], getPlayer()), event -> mailArray[finalI].onMailClick(getPlayer()));
            }
            getGui().addItem(guiItems);
            getGui().update();
        });

        getGui().open(getPlayer());
    }
}
