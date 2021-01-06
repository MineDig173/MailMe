package com.haroldstudios.mailme.database;

import com.haroldstudios.mailme.mail.Mail;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerMailDAO {

    CompletableFuture<Mail[]> getAllMail(final UUID uuid);
    CompletableFuture<Mail[]> getUnreadMail(final UUID uuid);
    CompletableFuture<Boolean> hasUnreadMail(final UUID uuid);
    CompletableFuture<Boolean> setUnread(final UUID uuid, Mail mail);
    CompletableFuture<Boolean> saveMailObj(Mail mail);
    CompletableFuture<Boolean> savePlayerMail(UUID uuid, Mail mail);

}
