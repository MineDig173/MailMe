package com.haroldstudios.mailme.gui.child;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.gui.AbstractMailGui;
import com.haroldstudios.mailme.gui.GuiOptions;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailItems;
import com.haroldstudios.mailme.mail.MailType;
import com.haroldstudios.mailme.utils.PlayerUtils;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.gui.guis.Gui;
import me.mattstudios.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ClickToSendGui extends AbstractMailGui {

    private final GuiItem sentItem;
    private boolean canSend = true;

    public ClickToSendGui(final MailMe plugin, final Mail.Builder<?> builder, final GuiOptions guiOptions) {
        super(plugin, new Gui(1, guiOptions.getTitle()), builder, guiOptions);
        getGui().setItem(1,9, new GuiItem(plugin.getLocale().getItemStack("gui.send-to-mailbox")));
        getGui().setItem(1,1, new GuiItem(Utils.getItemFromBuilder(getBuilder(), getPlayer())));
        GuiItem notSent = new GuiItem(plugin.getLocale().getItemStack("gui.not-sent"), event -> sendMail());
        for ( int i = 2; i < 9; i++) {
            getGui().setItem(1,i, notSent);
        }
        sentItem = new GuiItem(getPlugin().getLocale().getItemStack("gui.sent"));
    }

    @Override
    protected void nextMenu() {
        if (getGui().getInventory().getViewers().contains(getPlayer())) {
            getGui().close(getPlayer());
        }
    }

    public static GuiOptions getDefaultGuiOptions(final Player player) {
        return new GuiOptions()
                .withRows(1)
                .withTitle(MailMe.getInstance().getLocale().getMessage("gui.titles.click-to-send"))
                .setForWhom(player);
    }

    // Returns if success or not
    private boolean takeMoney(MailType type) {
        if (MailMe.getInstance().getVaultHook() == null) {
            return true;
        }
        return MailMe.getInstance().getVaultHook().attemptTransaction(getPlayer(), type);
    }

    public void sendMail() {

        if (!canSend) return;
        canSend = false;

        MailType type = MailType.getMailTypeFromMail(getBuilder());
        if (!takeMoney(type)) return;

        gracefulExit = true;
        int delay = 0;
        for (int i = 2; i < 9; i++) {
            setSent(i, delay);
            delay+=2;
        }
    }

    public void setSent(int col, int delay) {
        if (col <= 1 || col > 8) return;
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            getGui().updateItem(1,col,sentItem);

            if (col == 8) {
                List<UUID> recipients = getBuilder().getRecipients();
                if (getBuilder() instanceof MailItems.Builder) {
                    if (recipients.size() >= 1) {
                        getBuilder().build().send(Collections.singletonList(recipients.get(0)));
                    }
                } else {
                    getBuilder().build().send(recipients);
                }
                PlayerUtils.playUISound(getPlayer());
                getPlayer().sendMessage(getPlugin().getLocale().getMessage(getPlayer(),"mail.sent"));
                Bukkit.getScheduler().runTaskLater(getPlugin(), this::next, 80L);
            }
        }, delay);
    }

    @Override
    public void open() {
        getGui().open(getPlayer());
    }
}