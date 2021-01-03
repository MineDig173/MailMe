package com.haroldstudios.mailme.database;

/**
 * Connector for transitioning between database types
 */
public interface DatabaseConnector {

    /*
    Database Design

    Table 1
     [player uuid] [mail uuid]       [read]  [timestamp_received]
      harry's-uuid    ae9dhn etc.     true
      harry's uuid    asubnji etc.    false

    Table 2
    [mail uuid] [mail object]
    ae9dhn etc.    obj

    Player Settings (JSON)


     */

    /**
     * Connect to database
     *
     * @return if success
     */
    boolean connect();

    /**
     * Disconnect from database
     */
    void disconnect();

    /**
     * Do we currently have a connection?
     *
     * @return If connected
     */
    boolean isConnected();
}
