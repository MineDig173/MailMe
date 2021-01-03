package com.haroldstudios.mailme;

import com.haroldstudios.mailme.database.DatabaseConnector;
import com.haroldstudios.mailme.database.DatabaseSettingsImpl;
import com.haroldstudios.mailme.database.json.DataCache;
import com.haroldstudios.mailme.database.sql.MySQLDatabase;
import com.haroldstudios.mailme.utils.Locale;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class MailMe extends JavaPlugin {

    private DatabaseConnector connector;
    private DataCache dataCache;
    private Locale locale;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        initDatabaseConnection();
        this.locale = new Locale(this);
        this.dataCache = new DataCache();

        CommandManager commandManager = new CommandManager(this);
        commandManager.getCompletionHandler().register("#locale", val -> new ArrayList<>(locale.getLanguageTokens()));


    }

    @Override
    public void onDisable() {
        connector.disconnect();
        // Plugin shutdown logic
    }

    // Creates a connection to sql database
    private void initDatabaseConnection() {
        ConfigurationSection databaseConfig = getConfig().getConfigurationSection("database");
        connector = new MySQLDatabase(new DatabaseSettingsImpl(
                databaseConfig.getString("host"),
                databaseConfig.getInt("port"),
                databaseConfig.getString("database_name"),
                databaseConfig.getString("username"),
                databaseConfig.getString("password"),
                databaseConfig.getBoolean("useSSL"),
                databaseConfig.getString("driver")));
        connector.connect();
    }

    public Locale getLocale() {
        return locale;
    }

    public DataCache getCache() {
        return dataCache;
    }

    public static MailMe getInstance() {
        return getPlugin(MailMe.class);
    }
}
