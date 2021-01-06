package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.utils.Utils;
import me.mattstudios.mfgui.gui.components.ItemBuilder;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Bukkit;
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
        getPlugin().getPlayerMailDAO().getAllMail(getPlayer().getUniqueId()).thenAccept(mailArray -> {
            GuiItem[] guiItems = new GuiItem[mailArray.length];
            for (int i = 0; i < mailArray.length; i++) {
                Mail mail = mailArray[i];
                ItemBuilder itemBuilder = ItemBuilder.from(Utils.getItemFromMail(mailArray[i], getPlayer()));
                itemBuilder.glow(!mail.isRead());

                guiItems[i] = new GuiItem(itemBuilder.build(), event -> {
                    if (!mail.isRead()) {
                        mail.setRead(true);
                        getPlugin().getPlayerMailDAO().setUnread(getPlayer().getUniqueId(), mail).thenAccept(bool -> {
                            if (bool) {
                                mail.onMailClick(getPlayer());
                                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> new InboxGui(getPlugin(), getPlayer(), getPreviousMenu(), getBuilder()).open(), 1L);
                            } else {
                                System.out.println("We fucked");
                            }
                            return;
                        });

                    }
                });
            }
            getGui().addItem(guiItems);
            getGui().update();
        });

        getGui().open(getPlayer());
    }
}
