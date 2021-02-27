package com.haroldstudios.mailme.mail;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public class MailBook extends Mail{

    private final ItemStack book;

    /**
     * Main class constructor
     *
     * @param icon           ItemStack to show in mailbox
     * @param sender         String of the sender e.g "Harry", "Server" etc
     * @param expiryTimeMins Minutes until the mail should expire and delete itself from existence!
     */
    protected MailBook(ItemStack icon, String sender, int expiryTimeMins, String identifier, ItemStack book, boolean archived, String commentary) {
        super(icon, sender, expiryTimeMins, identifier, archived, commentary);
        this.book = book;
    }

    @Override
    public boolean onMailClick(Player whoClicked) {
        whoClicked.openBook(book);
        return true;
    }

    @Override
    public String[] getContentsAsString() {
        return getContents(book);
    }

    private static String[] getContents(ItemStack book) {
        ItemMeta meta = book.getItemMeta();
        if (!(meta instanceof BookMeta)) throw new IllegalArgumentException("Item was provided but does not have a book meta!");
        BookMeta bookMeta = (BookMeta) meta;
        if (!bookMeta.hasPages()) return new String[0];

        return new String[]{ChatColor.GRAY + bookMeta.getTitle()};
    }

    public static class Builder extends Mail.Builder<MailBook.Builder>{

        private ItemStack book;

        public void setBook(ItemStack book) {
            this.book = book;
        }

        @Override
        public List<ItemStack> getInputtedItems() {
            return Collections.singletonList(book);
        }

        @Override
        public String[] getContents() {
            return MailBook.getContents(book);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Mail build() {
            return new MailBook(getIcon(), getSender(), getExpiryTimeMins(), getIdentifier(), book, isArchived(), getCommentary());
        }
    }
}