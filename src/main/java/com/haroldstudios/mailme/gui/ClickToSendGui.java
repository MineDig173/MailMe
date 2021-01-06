package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ClickToSendGui extends AbstractMailGui {

    public ClickToSendGui(MailMe plugin, Player player, @Nullable AbstractMailGui previousMenu, Mail.Builder<?> builder) {
        super(plugin, player, previousMenu, 1, plugin.getLocale().getMessage(player, "gui.titles.click-to-send"), builder);
        getGui().getFiller().fill(getFillerItem());
    }

    @Override
    void nextMenu() {
        getGui().close(getPlayer());
    }

    @Override
    public void open() {
        getGui().setItem(1,5, new GuiItem(Utils.getItemFromBuilder(getBuilder(), getPlayer()), event -> {
            List<UUID> recipients = getBuilder().getRecipients();
            getBuilder().build().send(recipients);
            playUISound();
            next();
            getPlayer().sendMessage(getPlugin().getLocale().getMessage(getPlayer(),"mail.sent"));
        }));

        getGui().open(getPlayer());
    }
}
