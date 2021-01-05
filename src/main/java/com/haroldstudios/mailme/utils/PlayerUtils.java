package com.haroldstudios.mailme.utils;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerSettings;

import java.util.UUID;

public class PlayerUtils {

    public static void notifyUnread(UUID uuid) {
        PlayerSettings playerSettings = MailMe.getInstance().getCache().getPlayerSettings(uuid);
        if (playerSettings.isReceivingNotifications()) {
            MailMe.getInstance().getLocale().trySendMessageToUUID(uuid, "mail.received");
        }
    }
}
