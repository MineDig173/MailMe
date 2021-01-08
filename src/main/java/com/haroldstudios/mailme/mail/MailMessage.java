package com.haroldstudios.mailme.mail;

import com.haroldstudios.mailme.MailMe;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MailMessage extends Mail {

    private final String message;

    public MailMessage(ItemStack icon, String sender, int expiryTimeMins, String identifier, String message) {
        super(icon, sender, expiryTimeMins, identifier);
        this.message = message;
    }

    @Override
    public void onMailClick(Player whoClicked) {
        String msg = MailMe.getInstance().getLocale().getMessage("mail.message-contents");
        msg = msg.replace("%sender%", getSender());
        msg = msg.replace("%contents%", getContentsAsString()[0]);
        msg = msg.replace("%player_name%", whoClicked.getName());
        whoClicked.sendMessage(msg);
    }

    @Override
    public BaseComponent[] getContentsAsText() {
        return new ComponentBuilder(message).create();
    }

    @Override
    public String[] getContentsAsString() {
        return new String[]{message};
    }

    public static class Builder extends Mail.Builder<Builder>{

        private String message;

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String[] getContents() {
            return new String[]{message};
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
