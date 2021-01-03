package com.haroldstudios.mailme.mail;

import org.bukkit.inventory.ItemStack;

public class MailMessage extends Mail {

    private String message;

    public MailMessage(ItemStack icon, String sender, int expiryTimeMins, String message) {
        super(icon, sender, expiryTimeMins);
        this.message = message;
    }

    @Override
    public void onMailClick() {

    }

    public static class Builder extends Mail.Builder<Builder>{

        private String message;

        public void setMessage(String message) {
            this.message = message;
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
