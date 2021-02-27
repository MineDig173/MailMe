package com.haroldstudios.mailme.mail;

import com.haroldstudios.mailme.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MailItems extends Mail {

    private final List<ItemStack> itemStackList;

    /**
     * Main class constructor
     *
     * @param icon           ItemStack to show in mailbox
     * @param sender         String of the sender e.g "Harry", "Server" etc
     * @param expiryTimeMins Minutes until the mail should expire and delete itself from existence!
     * @param itemStackList  Items for this mail type to hold
     */
    protected MailItems(ItemStack icon, String sender, int expiryTimeMins, String identifier, boolean archived, List<ItemStack> itemStackList, String commentary) {
        super(icon, sender, expiryTimeMins, identifier, archived, commentary);
        this.itemStackList = itemStackList;
    }

    @Override
    public boolean onMailClick(Player whoClicked) {
        for (ItemStack item : itemStackList) {
            Utils.giveItem(whoClicked, item);
        }
        return true;
    }

    @Override
    public String[] getContentsAsString() {
        List<String> contents = new ArrayList<>();
        for (ItemStack stack : itemStackList) {
            contents.add(ChatColor.GRAY + "x" + stack.getAmount() + " " + Utils.getItemName(stack));
        }
        return contents.toArray(new String[0]);
    }

    public List<ItemStack> getItemStackList() {
        return itemStackList;
    }

    public static class Builder extends Mail.Builder<MailItems.Builder>{

        private List<ItemStack> itemStackList = new ArrayList<>();

        public MailItems.Builder setItemStackList(List<ItemStack> items) {
            this.itemStackList = items;
            return this;
        }

        @Override
        public List<ItemStack> getInputtedItems() {
            return itemStackList;
        }

        @Override
        public String[] getContents() {
            List<String> contents = new ArrayList<>();
            for (ItemStack stack : itemStackList) {
                contents.add(ChatColor.GRAY + "x" + stack.getAmount() + " " + Utils.getItemName(stack));
            }
            return contents.toArray(new String[0]);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public MailItems build() {
            return new MailItems(icon, sender, expiryTimeMins, getIdentifier(), archived, itemStackList, getCommentary());
        }
    }
}