package com.haroldstudios.mailme;

import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.database.json.DataCache;

@SuppressWarnings("unused")
public interface MailMeAPI {

    /**
     * Retrieves the in-use Data access object interface
     * for using the mail database.
     *
     * @return PlayerMailDAO {@link com.haroldstudios.mailme.database.PlayerMailDAO}
     */
    PlayerMailDAO getPlayerMailDAO();

    /**
     * Retrieves the on-server cache that stores per-server
     * data such as mailbox locations.
     *
     * @return DataCache {@link com.haroldstudios.mailme.database.json.DataCache}
     */
    DataCache getCache();
}