package com.haroldstudios.mailme.database.sql;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.DatabaseConnector;
import com.haroldstudios.mailme.database.DatabaseSettingsImpl;
import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.mail.Mail;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public abstract class SQLDatabaseParent implements DatabaseConnector, PlayerMailDAO {

    protected final Logger logger;
    protected Connection connection = null;
    @Nullable protected final DatabaseSettingsImpl settings;
    protected String connectionUrl;

    public SQLDatabaseParent(@Nullable DatabaseSettingsImpl settings, String connectionUrl) {
        this.settings = settings;
        logger = Bukkit.getLogger();
        this.connectionUrl = connectionUrl;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean connect() {
        if (isConnected()) return true;
        if (!loadDriver()) return false;

         try {
            if (settings != null) {
                logger.info(String.format("Beginning connection to SQL server at host: %s, port: %s, database_name: %s, using SSL: %s", settings.getHost(), settings.getPort(), settings.getDatabaseName(), settings.useSSL()));
                connection = DriverManager.getConnection(connectionUrl, settings.getUsername(), settings.getPassword());
            }
            assertTableReady();
            return true;
        } catch (SQLException e) {
             logger.severe("Could not connect to the database! " + e.getMessage());
             e.printStackTrace();
             getServer().getPluginManager().disablePlugin(MailMe.getInstance());
        }
        return false;
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                // If successfully closed, we can set it to null.
                if (connection.isClosed())
                    connection = null;
            } catch (SQLException e) {
                logger.severe("Could not disconnect from database. Error code: " + e.getErrorCode() + ", " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

    @Override
    public CompletableFuture<Boolean> hasUnreadMail(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select mail_uuid from PlayerMail where uuid='" + uuid.toString() + "' and mail_read=false");

                return resultSet.next();

            } catch (SQLException ignore) {}
            return false;
        }).exceptionally(e -> {
            MailMe.debug(SQLDatabaseParent.class, "Error occurred while retrieving data for " + uuid.toString() + ", " + e.getMessage());
            MailMe.debug(e);
            return false;
        });
    }

    @Override
    public CompletableFuture<Mail[]> getUnreadMail(UUID uuid) {
        return getMailFromQuery("select mail_uuid, mail_read, id, ts from PlayerMail where uuid='" + uuid.toString() + "' and mail_read=false");
    }

    @Override
    public CompletableFuture<Set<String>> getPresetMailIdentifiers() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select identifier_name from Mail");
                Set<String> identifiers = new HashSet<>();
                while (resultSet.next()) {
                    String id = resultSet.getString("identifier_name");
                    if (id == null) continue;
                    if (id.equals("null")) continue;
                    identifiers.add(id);
                }

                return identifiers;

            } catch (SQLException ignore) {}

            return new HashSet<>();
        });
    }

    @Override
    public CompletableFuture<Mail[]> getAllMail(UUID uuid) {
        return getMailFromQuery("select mail_uuid, mail_read, id, ts from PlayerMail where uuid='" + uuid.toString() + "'");
    }

    public CompletableFuture<Mail[]> getMailFromQuery(String query) {
        return CompletableFuture.supplyAsync(() -> {
            List<Mail> mail = new ArrayList<>();
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet == null) {
                    MailMe.debug(SQLDatabaseParent.class, "Result set was null!");
                    return new Mail[0];
                }
                // Loop through all instances of the player's uuid
                while (resultSet.next()) {
                    String mailId = resultSet.getString("mail_uuid");
                    Statement mailStmt = connection.createStatement();
                    ResultSet mailRS = mailStmt.executeQuery("select mail_obj from Mail where mail_uuid='" + mailId + "'");
                    // Grab from Mail table the uuid we found in our playermail table
                    if (mailRS.next()) {
                        // deserialize our object
                        Mail mailObj = MailMe.getInstance().getCache().getFileUtil().deserializeMail(mailRS.getString("mail_obj"));
                        mailObj.setRead(resultSet.getBoolean("mail_read"));
                        mailObj.setColId(resultSet.getInt("id"));
                        long ts = resultSet.getLong("ts");
                        mailObj.setDateReceived(ts);
                        if (!isExpired(mailObj, ts))
                            mail.add(mailObj);

                    } else {
                        MailMe.debug(SQLDatabaseParent.class, "Tried to access mail object " + mailId + " in table but it does not exist!");
                    }
                }

                Collections.reverse(mail);

            } catch (SQLException e) {
                MailMe.debug(SQLDatabaseParent.class, "Error occurred while retrieving data. Error code: " + e.getErrorCode() + ", " + e.getMessage());
            }
            return mail.toArray(new Mail[0]);
        }).exceptionally(e -> {
            MailMe.debug(e);
            return new Mail[0];
        });

    }

    @Override
    public CompletableFuture<Boolean> saveMailObj(Mail mail) {
        return CompletableFuture.supplyAsync(() -> {

            Statement statement;
            int sCode = 0;
            try {
                statement = connection.createStatement();
                String json = MailMe.getInstance().getCache().getFileUtil().serialize(mail);
                sCode = statement.executeUpdate("insert into Mail(mail_uuid, mail_obj, identifier_name) values ('"+mail.getUuid()+"', '"+json+"', '"+mail.getIdentifier()+"')");

                statement.close();
            } catch (SQLException ignore) {}

            // ExecuteUpdate on sql returns 0 if nothing was returned from server.
            return sCode > 0 ;
        }).exceptionally(e -> {
            MailMe.debug(e);
            return false;
        });
    }

    @Override
    public CompletableFuture<Boolean> savePlayerMail(UUID uuid, Mail mail) {
        return CompletableFuture.supplyAsync(() -> {
            int sCode = 0;
            try {
                Statement statement = connection.createStatement();
                sCode = statement.executeUpdate("insert into PlayerMail(uuid, mail_uuid, ts) values ('"+uuid+"','"+mail.getUuid().toString()+"', "+System.currentTimeMillis()+")");


                statement.close();

            } catch (SQLException ignore) {}

            return sCode > 0;

        }).exceptionally(e -> {
            MailMe.debug(e);
            return false;
        });
    }

    @Override
    public CompletableFuture<Boolean> setUnread(UUID uuid, Mail mail) {
        return CompletableFuture.supplyAsync(() -> {
            if (mail.getColId() == null) return false;
            int sCode = 0;
            try {
                Statement statement = connection.createStatement();
                sCode = statement.executeUpdate("update PlayerMail set mail_read = true where id = "+ mail.getColId());
                statement.close();

            } catch (SQLException ignore) { }

            return sCode > 0;

        }).exceptionally(exception -> {
            MailMe.debug(exception);
            return false;
        });
    }

    @Override
    public CompletableFuture<Mail> getPresetMail(String presetName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from Mail where identifier_name='"+presetName+"'");
                if (!resultSet.next()) return null;
                Mail mailObj = MailMe.getInstance().getCache().getFileUtil().deserializeMail(resultSet.getString("mail_obj"));

                statement.close();
                return mailObj;

            } catch (SQLException ignore) { }
            return null;
        }).exceptionally(e -> {
            MailMe.debug(e);
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> deletePresetMail(String presetName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Statement statement = connection.createStatement();
                int result = statement.executeUpdate("delete from Mail where identifier_name='"+presetName+"'");
                statement.close();

                return result > 0;

                // Exceptions do not get caught during execution of completablefuture. Must be caught using exceptionally
            } catch (SQLException ignore) { }
            return false;
        }).exceptionally(e -> {
            MailMe.debug(e);
            return false;
        });
    }

    @Override
    public void deletePlayerMail(UUID uuid, Mail mail) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("delete from PlayerMail where id="+mail.getColId());
            statement.close();

        } catch (SQLException throwables) {
            MailMe.debug(throwables);
        }
    }

    private boolean isExpired(Mail mail, long timeMillis) {

        boolean expired = timeMillis + mail.getExpiryTimeMilliSeconds() < System.currentTimeMillis();
        if (expired) {
            CompletableFuture.runAsync(() -> {
                try {
                    Statement statement = connection.createStatement();
                    statement.executeUpdate("delete from PlayerMail where id=" + mail.getColId());
                    statement.close();

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }).exceptionally(e -> {
                MailMe.debug(SQLDatabaseParent.class, e.getMessage());
                return null;
            });
        }

        return expired;
    }

    @Override
    public CompletableFuture<Boolean> savePreset(Mail mail) {
        return saveMailObj(mail);
    }

    /**
     * Creates any missing tables and columns
     */
    public void assertTableReady() {

        try {
            Statement stmt = connection.createStatement();
            // Create Tables if not already there
            stmt.execute("create table if not exists PlayerMail(id int auto_increment primary key, uuid varchar(36) not null, mail_uuid varchar(36) not null, mail_read tinyint(1) not null default false, ts bigint not null)");
            stmt.execute("create table if not exists Mail(mail_uuid varchar(36) not null, mail_obj mediumtext not null, identifier_name varchar(36))");

        } catch (SQLException e) {
            MailMe.debug(SQLDatabaseParent.class, "Error occurred while asserting table readiness. Error code: " + e.getErrorCode() + ", " + e.getMessage());
            MailMe.debug(e);
        }
    }

    /**
     * Attempts to load SQL driver
     *
     * @return If Successful
     */
    public boolean loadDriver() {
        if (settings == null) return true;
        try {
            Class.forName(settings.getDriver());
        } catch (Exception e) {
            MailMe.debug(SQLDatabaseParent.class, "Failed to load driver \"" + settings.getDriver() + "\" when loading the SQLDatabase");
            MailMe.debug(e);
            return false;
        }
        return true;
    }
}
