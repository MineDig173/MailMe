package com.haroldstudios.mailme.mail;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class MailConsoleCommand extends Mail {

    private final String command;

    /**
     * Main class constructor
     *
     * @param icon           ItemStack to show in mailbox
     * @param sender         String of the sender e.g "Harry", "Server" etc
     * @param expiryTimeMins Minutes until the mail should expire and delete itself from existence!
     * @param identifier     ID for preset
     */
    protected MailConsoleCommand(ItemStack icon, String sender, int expiryTimeMins, String identifier, String command) {
        super(icon, sender, expiryTimeMins, identifier);
        this.command = command;
    }

    @Override
    public boolean onMailClick(Player whoClicked) {
        return false;
    }

    @Override
    public BaseComponent[] getContentsAsText() {
        return new BaseComponent[0];
    }

    @Override
    public String[] getContentsAsString() {
        return new String[0];
    }

    public static class Builder extends Mail.Builder<MailConsoleCommand.Builder>{

        String command = "";

        @Override
        public List<ItemStack> getInputtedItems() {
            return Collections.emptyList();
        }

        @Override
        public String[] getContents() {
            return new String[]{command};
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Mail build() {
            return new MailConsoleCommand(getIcon(), getSender(), getExpiryTimeMins(), getIdentifier(), getCommand());
        }
    }


}
