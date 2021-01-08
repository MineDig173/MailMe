package com.haroldstudios.mailme.database.sql;

import com.haroldstudios.mailme.database.DatabaseSettingsImpl;

public class MySQLDatabase extends SQLDatabaseParent {


    public MySQLDatabase(DatabaseSettingsImpl settings) {
        super(settings,
                "jdbc:mysql://" + settings.getHost() +
                        ":" + settings.getPort() +
                        "/" + settings.getDatabaseName() +
                        "?autoReconnect=true&useSSL=" + settings.useSSL() +
                        "&allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8" + settings.getAdditionalUrl()
                );
    }
}
