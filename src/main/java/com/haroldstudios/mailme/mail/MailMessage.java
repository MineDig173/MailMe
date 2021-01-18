package com.haroldstudios.mailme.mail;

import com.haroldstudios.mailme.MailMe;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class MailMessage extends Mail {

    private final String message;

    public MailMessage(ItemStack icon, String sender, int expiryTimeMins, String identifier, String message) {
        super(icon, sender, expiryTimeMins, identifier);
        this.message = message;
    }

    @Override
    public boolean onMailClick(Player whoClicked) {
        String msg = MailMe.getInstance().getLocale().getMessage("mail.message-contents");
        msg = msg.replace("%sender%", getSender());
        msg = msg.replace("%contents%", ChatColor.GRAY + getContentsAsString()[0]);
        msg = msg.replace("%player_name%", whoClicked.getName());
        whoClicked.sendMessage(msg);
        return true;
    }

    @Override
    public String[] getContentsAsString() {
        return new String[]{ChatColor.GRAY + message};
    }

    public static class Builder extends Mail.Builder<Builder>{

        private String message;

        public MailMessage.Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public List<ItemStack> getInputtedItems() {
            return Collections.emptyList();
        }

        @Override
        public String[] getContents() {
            return new String[]{ChatColor.GRAY + message};
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public MailMessage build() {

            return new MailMessage(icon, sender, expiryTimeMins, identifier, message);
        }
    }
}
