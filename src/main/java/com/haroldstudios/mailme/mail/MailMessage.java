package com.haroldstudios.mailme.mail;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MailMessage extends Mail {

    private final String message;

    public MailMessage(ItemStack icon, String sender, int expiryTimeMins, String message) {
        super(icon, sender, expiryTimeMins);
        this.message = message;
    }

    @Override
    public void onMailClick(Player whoClicked) {
        whoClicked.sendMessage(getContentsAsString());
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

            return new MailMessage(icon, sender, expiryTimeMins, message);
        }
    }
}
