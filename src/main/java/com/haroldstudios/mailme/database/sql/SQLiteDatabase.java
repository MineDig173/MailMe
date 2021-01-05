package com.haroldstudios.mailme.database.sql;

import com.haroldstudios.mailme.MailMe;

import java.io.File;

public class SQLiteDatabase extends SQLDatabaseParent {

    public SQLiteDatabase() {
        super(null, "");

        File dataFolder = new File(MailMe.getInstance().getDataFolder(), "flatfile_database");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            MailMe.getInstance().getLogger().severe("Could not create database folder!");
            return;
        }
        super.connectionUrl = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + File.separator + "database.db";
    }
}
