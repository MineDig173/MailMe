/*
 *   Copyright [2020] [Harry0198]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.haroldstudios.mailme.conversations;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.gui.ClickToSendGui;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailMessage;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public final class LetterInputPrompt extends StringPrompt {

    private final Mail.Builder mail;

    public LetterInputPrompt(Mail.Builder mail) {
        this.mail = mail;
    }

    public String getPromptText(ConversationContext context) {
        Player player = (Player) context.getForWhom();
        return MailMe.getInstance().getLocale().getMessage(player, "mail.message");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String s) {

        if (s == null || s.equalsIgnoreCase("cancel")) {
            return Prompt.END_OF_CONVERSATION;
        }

        if (mail instanceof MailMessage.Builder) {
            ((MailMessage.Builder) mail).setMessage(s);
            new ClickToSendGui(MailMe.getInstance(), ((Player) context.getForWhom()), null, mail).open();
            return Prompt.END_OF_CONVERSATION;
        }
        return Prompt.END_OF_CONVERSATION;
    }

    public static void begin(MailMe plugin, Mail.Builder builder, Player player) {
       new ConversationFactory(plugin).withModality(true)
                .withFirstPrompt(new LetterInputPrompt(builder))
                .withEscapeSequence("cancel").withTimeout(300)
                .addConversationAbandonedListener(new ConversationAbandonedListener())
                .thatExcludesNonPlayersWithMessage("Console is not supported by this command")
                .buildConversation(player)
                .begin();
    }
}
