package com.haroldstudios.mailme.conversations;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.gui.child.ClickToSendGui;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailConsoleCommand;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public final class ConsoleMailInput extends StringPrompt {

    private final Mail.Builder<?> mail;
    private Runnable runnable;

    public ConsoleMailInput(Mail.Builder<?> mail, Player player) {
        this.mail = mail;
        this.runnable = () -> new ClickToSendGui(MailMe.getInstance(), mail, ClickToSendGui.getDefaultGuiOptions(player)).open();
    }

    public String getPromptText(ConversationContext context) {
        Player player = (Player) context.getForWhom();
        return MailMe.getInstance().getLocale().getMessage(player, "mail.console");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String s) {

        if (s == null || s.equalsIgnoreCase("cancel")) {
            return Prompt.END_OF_CONVERSATION;
        }
        if (mail instanceof MailConsoleCommand.Builder) {
            ((MailConsoleCommand.Builder) mail).setCommand(s);
        }

        runnable.run();
        return Prompt.END_OF_CONVERSATION;
    }

    public ConsoleMailInput withRunnable(Runnable runnable) {
        if (runnable == null) return this;
        this.runnable = runnable;
        return this;
    }

    public static void begin(MailMe plugin, Mail.Builder<?> builder, Player player, Runnable runnable) {
        new ConversationFactory(plugin).withModality(true)
                .withFirstPrompt(new ConsoleMailInput(builder, player).withRunnable(runnable))
                .withEscapeSequence("cancel").withTimeout(300)
                .thatExcludesNonPlayersWithMessage("Console is not supported by this command")
                .buildConversation(player)
                .begin();
    }
}