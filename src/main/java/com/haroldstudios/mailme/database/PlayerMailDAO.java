package com.haroldstudios.mailme.database;

import com.haroldstudios.mailme.mail.Mail;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerMailDAO {

    // NOTE: We actually do not typically assert the order of the elements.
    // Inside some of the handlers we reverse the array or collections generally.
    // order is not confirmed and if wanted should be sorted manually.

    /**
     * Gets all Mail for the desired UUID.
     * If an internal error occurs, the mail array with be empty.
     *
     * @param uuid UUID Player's Unique ID
     * @return CompletableFuture<Mail[]> {@link com.haroldstudios.mailme.mail.Mail}
     */
    CompletableFuture<Mail[]> getAllMail(final UUID uuid);

    /**
     * Gets unread mail for the desired UUID.
     *
     * @param uuid UUID Player's Unique ID
     * @return CompletableFuture<Mail[]> {@link com.haroldstudios.mailme.mail.Mail}
     */
    CompletableFuture<Mail[]> getUnreadMail(final UUID uuid);

    /**
     * Checks if player has unread mail for the desired UUID.
     *
     * @param uuid UUID Player's Unique ID
     * @return CompletableFuture<Boolean> True if has unread mail,
     * False if player does not have unread mail or an internal error occurs
     */
    CompletableFuture<Boolean> hasUnreadMail(final UUID uuid);

    /**
     * Sets the mail to the read state
     *
     * @param uuid UUID Player's Unique ID
     * @param mail Mail {@link com.haroldstudios.mailme.mail.Mail} to set read state for
     * @return
     */
    CompletableFuture<Boolean> setRead(final UUID uuid, Mail mail);

    CompletableFuture<Boolean> setArchived(final UUID uuid, Mail mail);

    /**
     * Saves a preset mail.
     * Different to {@link #saveMailObj(Mail)} as this saves in unique locations dependant on database type
     *
     * @param mail Mail {@link com.haroldstudios.mailme.mail.Mail} to save
     * @return
     */
    CompletableFuture<Boolean> savePreset(final Mail mail);

    /**
     * Gets preset mail from database with identifier
     *
     * @param presetName Identifier of mail
     * @return Mail {@link com.haroldstudios.mailme.mail.Mail} preset
     */
    CompletableFuture<Mail> getPresetMail(final String presetName);

    /**
     * Deletes the preset mail with identifier
     *
     * @param presetName Identifier of mail
     * @return True if success, false if fail
     */
    CompletableFuture<Boolean> deletePresetMail(final String presetName);

    /**
     * Gets all of the preset mail identifiers
     *
     * @return Set<String> of identifiers
     */
    CompletableFuture<Set<String>> getPresetMailIdentifiers();

    /**
     * Deletes player's mail from database
     *
     * @param uuid UUID Player's Unique ID
     * @param mail Mail {@link com.haroldstudios.mailme.mail.Mail} to delete
     */
    void deletePlayerMail(UUID uuid, final Mail mail);
    // We don't care what it returns.
    CompletableFuture<?> deletePlayerMail(UUID uuid, final Mail[] mail);

    /**
     * Saves a mail object to database
     *
     * @param mail Mail {@link com.haroldstudios.mailme.mail.Mail}} to save
     * @return True if success, False if failure
     */
    CompletableFuture<Boolean> saveMailObj(Mail mail);

    /**
     * Saves PlayerMail to database
     * Will fail if mail object is not present in database already.
     * Can be entered using {@link #saveMailObj(Mail)}
     *
     * @param uuid UUID Player's Unique ID
     * @param mail Mail {@link com.haroldstudios.mailme.mail.Mail}} to save
     * @return True if success, False if failure
     */
    CompletableFuture<Boolean> savePlayerMail(UUID uuid, Mail mail);

}