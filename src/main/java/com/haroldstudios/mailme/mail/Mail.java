package com.haroldstudios.mailme.mail;

import com.haroldstudios.mailme.MailMe;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public abstract class Mail {

    private final UUID uuid;
    private final long dateCreated;
    private final int expiryTimeMins;
    private final String sender;
    private final ItemStack icon;

    /**
     * Main class constructor
     *
     * @param icon ItemStack to show in mailbox
     * @param sender String of the sender e.g "Harry", "Server" etc
     * @param expiryTimeMins Minutes until the mail should expire and delete itself from existence!
     */
    protected Mail(ItemStack icon, String sender, int expiryTimeMins) {
        this.uuid = UUID.randomUUID();
        this.icon = icon;
        this.sender = sender;
        this.expiryTimeMins = expiryTimeMins;
        this.dateCreated = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return uuid;
    }

    public abstract void onMailClick();

    // Hierarchical Builder Pattern
    protected abstract static class Builder<T extends Builder<T>>{

        protected String sender;
        protected ItemStack icon;
        protected int expiryTimeMins;
        private Long dateCreated;

        public T setSender(String sender) {
            this.sender = sender;
            return self();
        }

        public T setIcon(ItemStack icon) {
            this.icon = icon;
            return self();
        }

        public T setExpiryTimeMins(int expiry) {
            this.expiryTimeMins = expiry;
            return self();
        }

        public T setDateCreated(long dateCreated) {
            this.dateCreated = dateCreated;
            return self();
        }

        public long getDateCreated() {
            if (dateCreated == null) return System.currentTimeMillis();
            return dateCreated;
        }

        // Subclasses must override and return 'this' (themselves)
        protected abstract T self();

        public abstract Mail build();
    }

}
