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
import com.haroldstudios.mailme.gui.RecipientSelectorGui;
import com.haroldstudios.mailme.mail.Mail;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public final class PlayerSearch extends StringPrompt {

    private final Mail.Builder mail;

    public PlayerSearch(Mail.Builder mail) {
        this.mail = mail;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return MailMe.getInstance().getLocale().getMessage((Player) context.getForWhom(), "gui.search");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String s) {
        if (s == null || s.equalsIgnoreCase("cancel")) {
            return Prompt.END_OF_CONVERSATION;
        }

        Player player = (Player) context.getForWhom();

        Bukkit.getScheduler().runTaskAsynchronously(MailMe.getInstance(), () -> {
            OfflinePlayer search = Bukkit.getOfflinePlayer(s);

            Bukkit.getScheduler().runTask(MailMe.getInstance(), () -> new RecipientSelectorGui(MailMe.getInstance(), player, null, mail, search).open());
        });

        return Prompt.END_OF_CONVERSATION;
    }

    public static void begin(MailMe plugin, Mail.Builder builder, Player player) {
        new ConversationFactory(plugin).withModality(true)
                .withFirstPrompt(new PlayerSearch(builder))
                .withEscapeSequence("cancel").withTimeout(300)
                .addConversationAbandonedListener(new ConversationAbandonedListener())
                .thatExcludesNonPlayersWithMessage("Console is not supported by this command")
                .buildConversation(player)
                .begin();
    }
}
