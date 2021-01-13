package com.haroldstudios.mailme.database.transition;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.DatabaseSettingsImpl;
import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.database.json.JsonDatabase;
import com.haroldstudios.mailme.database.sql.MySQLDatabase;
import com.haroldstudios.mailme.mail.Mail;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;


public class MySQL2Json implements Transitionable {

    private final MailMe plugin;
    private final PlayerMailDAO jsonDatabase;
    private final MySQLDatabase connector;

    public MySQL2Json(final MailMe plugin) {
        this.plugin = plugin;
        this.jsonDatabase = plugin.getPlayerMailDAO();

        if (!(this.jsonDatabase instanceof JsonDatabase)) {
            plugin.getLogger().warning("Invalid database transition! Trying to convert from json to " + jsonDatabase.getClass() + " using the " + MySQL2Json.class + " transitionable!");
        }

        ConfigurationSection databaseConfig = plugin.getConfig().getConfigurationSection("database");

        connector = new MySQLDatabase(new DatabaseSettingsImpl(
                databaseConfig.getString("host"),
                databaseConfig.getInt("port"),
                databaseConfig.getString("database_name"),
                databaseConfig.getString("username"),
                databaseConfig.getString("password"),
                databaseConfig.getBoolean("useSSL"),
                databaseConfig.getString("driver"),
                databaseConfig.getString("additional_url")));
    }

    @Override
    public void transitionMail() {

        if (!connector.connect()) {
            plugin.getLogger().warning("Cannot get connection to mysql database to convert from.");
            return;
        }

        connector.getMailFromQuery("select * from PlayerMail").thenAccept(mailArr -> {
            for (Mail mail : mailArr) {

                try {
                    Statement statement = connector.getConnection().createStatement();
                    ResultSet resultSet = statement.executeQuery("select * from PlayerMail where id="+mail.getColId());

                    if (!resultSet.next()) continue;
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));

                    jsonDatabase.saveMailObj(mail);
                    jsonDatabase.savePlayerMail(uuid, mail);

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });

        connector.getPresetMailIdentifiers().thenAccept(identifiers -> {
            for (String identifier : identifiers) {

                connector.getPresetMail(identifier).thenAccept(mail -> {
                    if (mail == null) {
                        MailMe.debug(Json2MySQL.class, "Couldn't retrieve preset with id: " + identifier + "! But it was already loaded!");
                        return;
                    }
                    jsonDatabase.savePreset(mail);
                });

            }
        });

        connector.disconnect();

    }
}
