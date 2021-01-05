package com.haroldstudios.mailme.mail;

import com.haroldstudios.mailme.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
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
    protected MailItems(ItemStack icon, String sender, int expiryTimeMins, List<ItemStack> itemStackList) {
        super(icon, sender, expiryTimeMins);
        this.itemStackList = itemStackList;
    }

    @Override
    public void onMailClick(Player whoClicked) {

    }

    @Override
    public BaseComponent[] getContentsAsText() {
        return new BaseComponent[0];
    }

    @Override
    public String[] getContentsAsString() {
        List<String> contents = new ArrayList<>();
        for (ItemStack stack : itemStackList) {
            contents.add("x" + stack.getAmount() + " " + Utils.getLocalizedName(stack));
        }
        return contents.toArray(new String[0]);
    }

    public static class Builder extends Mail.Builder<MailItems.Builder>{

        private List<ItemStack> itemStackList = new ArrayList<>();

        public void setItemStackList(List<ItemStack> items) {
            this.itemStackList = items;
        }

        @Override
        public String[] getContents() {
            return new String[0];
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Mail build() {
            return new MailItems(icon, sender, expiryTimeMins, itemStackList);
        }
    }
}