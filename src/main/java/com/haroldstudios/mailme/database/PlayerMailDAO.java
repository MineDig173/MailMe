package com.haroldstudios.mailme.database;

import com.haroldstudios.mailme.mail.Mail;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerMailDAO {

    // NOTE: We actually do not typically assert the order of the elements.
    // Inside some of the handlers we reverse the array or collections generally.
    // order is not confirmed and if wanted should be sorted manually.
    CompletableFuture<Mail[]> getAllMail(final UUID uuid);
    CompletableFuture<Mail[]> getUnreadMail(final UUID uuid);
    CompletableFuture<Boolean> hasUnreadMail(final UUID uuid);
    CompletableFuture<Boolean> setUnread(final UUID uuid, Mail mail);
    CompletableFuture<Boolean> savePreset(final Mail mail);
    CompletableFuture<Mail> getPresetMail(final String presetName);
    CompletableFuture<Boolean> deletePresetMail(final String presetName);
    CompletableFuture<Set<String>> getPresetMailIdentifiers();
    void deletePlayerMail(UUID uuid, final Mail mail);
    CompletableFuture<Boolean> saveMailObj(Mail mail);
    CompletableFuture<Boolean> savePlayerMail(UUID uuid, Mail mail);

}
