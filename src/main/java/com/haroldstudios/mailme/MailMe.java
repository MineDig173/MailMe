package com.haroldstudios.mailme;

import com.haroldstudios.mailme.commands.MailCommands;
import com.haroldstudios.mailme.commands.MailboxCommands;
import com.haroldstudios.mailme.commands.PostOfficeCommands;
import com.haroldstudios.mailme.commands.PresetCommands;
import com.haroldstudios.mailme.components.hooks.HologramHook;
import com.haroldstudios.mailme.components.hooks.VaultHook;
import com.haroldstudios.mailme.database.DatabaseConnector;
import com.haroldstudios.mailme.database.DatabaseSettingsImpl;
import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.database.json.DataCache;
import com.haroldstudios.mailme.database.json.JsonDatabase;
import com.haroldstudios.mailme.database.sql.MySQLDatabase;
import com.haroldstudios.mailme.listeners.EntityEvents;
import com.haroldstudios.mailme.mail.MailboxTaskManager;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.Locale;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.logging.Level;

public final class MailMe extends JavaPlugin {

    private VaultHook vaultHook;
    private HologramHook hologramHook;

    private MailboxTaskManager mailboxTaskManager;
    @Nullable private DatabaseConnector connector;
    private PlayerMailDAO playerMailDAO;
    private DataCache dataCache;
    private Locale locale;

    private EntityEvents listener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        ConfigValue.load(this);
        initDatabaseConnection();
        if (getServer().getPluginManager().getPlugin("Vault") != null && ConfigValue.HOOK_VAULT_ENABLED) {
            this.vaultHook = new VaultHook(this);
        }

        if (getServer().getPluginManager().getPlugin("HolographicDisplays") != null && ConfigValue.HOOK_HOLOGRAMS_ENABLED) {
            this.hologramHook = new HologramHook(this);
        }
        this.locale = new Locale(this);
        this.dataCache = new DataCache();

        CommandManager commandManager = new CommandManager(this);
        commandManager.getCompletionHandler().register("#locale", val -> new ArrayList<>(locale.getLanguageTokens()));

        commandManager.register(new MailCommands(this));
        commandManager.register(new MailboxCommands(this));
        commandManager.register(new PostOfficeCommands(this));
        commandManager.register(new PresetCommands(this));

        commandManager.hideTabComplete(true);
        listener = new EntityEvents(this);
        getServer().getPluginManager().registerEvents(listener, this);

        this.mailboxTaskManager = new MailboxTaskManager(this);
        this.mailboxTaskManager.beginTasks();


        if (getConfig().getInt("config-ver") <= 2) {
            getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "WARNING!");
            getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "MailMe has detected being upgraded! Before you continue, please read the following note;");
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of MailMe is incompatible with previous types of MailMe data! " +
                    "To fully upgrade you MUST delete the MailMe folder and RESTART. Please note; this will delete ALL player mail and settings and language files. If you have customized " +
                    "the language folders. Please save them separately and you may re import them or re translate with the new messages. They are not the same.");
            getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "Please view the wiki at https://wiki.haroldstudios.com for more information on why, how and other help / options!");
            getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "WARNING!");
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(listener);
        if (connector != null)
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
                    databaseConfig.getString("driver"),
                    databaseConfig.getString("additional_url")));
            playerMailDAO = (PlayerMailDAO) connector;
            connector.connect();
        } else {
            connector = null;
            playerMailDAO = new JsonDatabase(this);
        }
    }

    public static void debug(Class<?> clazz, String msg) {
        if (!ConfigValue.DEBUG) return;
        Bukkit.getLogger().log(Level.INFO, String.format("%s: Class: %s", msg, clazz.getName()));
    }

    public static void debug(Exception exception) {
        if (!ConfigValue.DEBUG) return;
        exception.printStackTrace();
    }

    public static void debug(Throwable throwable) {
        if (!ConfigValue.DEBUG) return;
        throwable.printStackTrace();
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public HologramHook getHologramHook() {
        return hologramHook;
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
