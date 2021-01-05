package com.haroldstudios.mailme;

import com.haroldstudios.mailme.commands.MailCommands;
import com.haroldstudios.mailme.commands.MailboxCommands;
import com.haroldstudios.mailme.database.DatabaseConnector;
import com.haroldstudios.mailme.database.DatabaseSettingsImpl;
import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.database.json.DataCache;
import com.haroldstudios.mailme.database.sql.MySQLDatabase;
import com.haroldstudios.mailme.database.sql.SQLiteDatabase;
import com.haroldstudios.mailme.listeners.EntityEvents;
import com.haroldstudios.mailme.mail.MailboxTaskManager;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.Locale;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class MailMe extends JavaPlugin {

    private MailboxTaskManager mailboxTaskManager;
    private DatabaseConnector connector;
    private PlayerMailDAO playerMailDAO;
    private DataCache dataCache;
    private Locale locale;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        ConfigValue.load(this);
        initDatabaseConnection();
        this.locale = new Locale(this);
        this.dataCache = new DataCache();

        CommandManager commandManager = new CommandManager(this);
        commandManager.getCompletionHandler().register("#locale", val -> new ArrayList<>(locale.getLanguageTokens()));

        commandManager.register(new MailCommands(this));
        commandManager.register(new MailboxCommands(this));

        getServer().getPluginManager().registerEvents(new EntityEvents(this), this);

        this.mailboxTaskManager = new MailboxTaskManager(this);
        this.mailboxTaskManager.beginTasks();
    }

    @Override
    public void onDisable() {
        connector.disconnect();
        mailboxTaskManager.stopTasks();
    }


    // Creates a connection to sql database
    private void initDatabaseConnection() {
        ConfigurationSection databaseConfig = getConfig().getConfigurationSection("database");
        if (databaseConfig == null) {
            getLogger().severe("Database connection was not made. Key: database was missing in config.yml");
            return;
        }

        if (databaseConfig.getString("type").equalsIgnoreCase("MYSQL")) {
            connector = new MySQLDatabase(new DatabaseSettingsImpl(
                    databaseConfig.getString("host"),
                    databaseConfig.getInt("port"),
                    databaseConfig.getString("database_name"),
                    databaseConfig.getString("username"),
                    databaseConfig.getString("password"),
                    databaseConfig.getBoolean("useSSL"),
                    databaseConfig.getString("driver")));
        } else {
            connector = new SQLiteDatabase();
        }
        playerMailDAO = (PlayerMailDAO) connector;
        connector.connect();
    }

    public Locale getLocale() {
        return locale;
    }

    public DataCache getCache() {
        return dataCache;
    }

    public PlayerMailDAO getPlayerMailDAO() {
        return playerMailDAO;
    }

    public static MailMe getInstance() {
        return getPlugin(MailMe.class);
    }
}
