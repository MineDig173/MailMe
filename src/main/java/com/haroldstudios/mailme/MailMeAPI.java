package com.haroldstudios.mailme;

import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.database.json.DataCache;

@SuppressWarnings("unused")
public class MailMeAPI {

    private final MailMe plugin;

    public MailMeAPI() {
        this.plugin = MailMe.getInstance();
    }

    /**
     * Retrieves the in-use Data access object interface
     * for using the mail database.
     *
     * @return PlayerMailDAO Interface for accessing database.
     */
    public PlayerMailDAO getPlayerMailDAO() {
        return plugin.getPlayerMailDAO();
    }

    /**
     * Retrieves the on-server cache that stores per-server
     * data such as mailbox locations.
     *
     * @return DataCache Cached data per server.
     */
    public DataCache getDataCache() {
        return plugin.getCache();
    }
}
