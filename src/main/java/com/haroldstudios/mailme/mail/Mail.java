package com.haroldstudios.mailme.mail;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.utils.PlayerUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
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

    public String getSender() {
        return sender;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void send(List<UUID> recipients) {
        PlayerMailDAO dao = MailMe.getInstance().getPlayerMailDAO();

        dao.saveMailObj(this);

        for (UUID recipient : recipients) {
            dao.savePlayerMail(recipient, this).thenAccept(success -> {
                PlayerUtils.notifyUnread(recipient);
            });
        }
    }

    public int getExpiryTimeMilliSeconds() { return expiryTimeMins * 60 * 1000; }

    public boolean isExpired() {
         return dateCreated + getExpiryTimeMilliSeconds() < System.currentTimeMillis();
    }

    public abstract void onMailClick(Player whoClicked);
    public abstract BaseComponent[] getContentsAsText();
    public abstract String[] getContentsAsString();

    // Hierarchical Builder Pattern
    public abstract static class Builder<T extends Builder<T>>{

        protected String sender;
        protected ItemStack icon;
        protected int expiryTimeMins;
        private Long dateCreated;
        private final List<UUID> recipients = new ArrayList<>();

        public T setSender(String sender) {
            this.sender = sender;
            return self();
        }

        public T setIcon(ItemStack icon) {
            this.icon = icon;
            return self();
        }

        public T addRecipient(UUID uuid) {
            this.recipients.add(uuid);
            return self();
        }

        public T removeRecipient(UUID uuid) {
            this.recipients.remove(uuid);
            return self();
        }

        public List<UUID> getRecipients() {
            return recipients;
        }

        public boolean isRecipient(UUID uuid) {
            return this.recipients.contains(uuid);
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

        public ItemStack getIcon() {
            return icon.clone();
        }

        public String getSender() {
            return sender;
        }

        public int getExpiryTimeMins() {
            return expiryTimeMins;
        }

        public Mail.Builder<?> combine(Mail.Builder<?> combineTo) {
            if (combineTo == null) return this;
            this.getRecipients().addAll(combineTo.getRecipients());
            this.setIcon(combineTo.getIcon());
            this.setSender(combineTo.getSender());
            this.setExpiryTimeMins(combineTo.getExpiryTimeMins());
            return this;
        }

        public abstract String[] getContents();

        // Subclasses must override and return 'this' (themselves)
        protected abstract T self();

        public abstract Mail build();
    }

}
