package com.haroldstudios.mailme.database;

import com.haroldstudios.mailme.mail.Mail;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerMailDAO {

    CompletableFuture<Mail[]> getUnreadMail(final UUID uuid);
    CompletableFuture<Boolean> hasUnreadMail(final UUID uuid);
    void saveMailObj(Mail mail);
    void savePlayerMail(UUID uuid, Mail mail);

}
