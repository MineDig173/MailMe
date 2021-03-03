package com.haroldstudios.mailme.database.mysql;

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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class MySQLDatabase implements DatabaseConnector, PlayerMailDAO {

    protected final Logger logger;
    protected Connection connection = null;
    @Nullable
    protected final DatabaseSettingsImpl settings;
    protected String connectionUrl;

    public MySQLDatabase(DatabaseSettingsImpl settings) {
        this.settings = settings;
        logger = Bukkit.getLogger();
        this.connectionUrl = "jdbc:mysql://" + settings.getHost() +
                        ":" + settings.getPort() +
                        "/" + settings.getDatabaseName() +
                        "?autoReconnect=true&useSSL=" + settings.useSSL() +
                        "&allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8" + settings.getAdditionalUrl();
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
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = connection.prepareStatement("SELECT mail_uuid FROM PlayerMail WHERE uuid = ? and mail_read = ?");
                setValues(preparedStatement, uuid.toString(), false);
                return preparedStatement.executeQuery().next();
            } catch (SQLException e) {
                MailMe.debug(e);
            } finally {
                closeStatement(preparedStatement);
            }
            return false;
        });
    }

    @Override
    public CompletableFuture<Mail[]> getUnreadMail(UUID uuid) {
        return getMailFromQuery("select mail_uuid, mail_read, id, ts, archived from PlayerMail where uuid='" + uuid.toString() + "' and mail_read=false");
    }

    @Override
    public CompletableFuture<Set<String>> getPresetMailIdentifiers() {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            try {
                statement = connection.prepareStatement("SELECT identifier_name FROM Mail");
                ResultSet resultSet = statement.executeQuery();
                Set<String> identifiers = new HashSet<>();
                while (resultSet.next()) {
                    String id = resultSet.getString("identifier_name");
                    if (id == null) continue;
                    if (id.equals("null")) continue;
                    identifiers.add(id);
                }
                return identifiers;
            } catch (SQLException e) {
                MailMe.debug(e);
            } finally {
                closeStatement(statement);
            }

            return new HashSet<>();
        });
    }

    @Override
    public CompletableFuture<Mail[]> getAllMail(UUID uuid) {
        return getMailFromQuery("SELECT mail_uuid, mail_read, id, archived, ts FROM PlayerMail WHERE uuid='" + uuid.toString() + "'");
    }

    @Override
    public CompletableFuture<Boolean> saveMailObj(Mail mail) {
        return CompletableFuture.supplyAsync(() -> {
            String json = MailMe.getInstance().getCache().getFileUtil().serialize(mail);
            return executeSQL("INSERT INTO Mail(mail_uuid, mail_obj, identifier_name) VALUES (?,?,?)", mail.getUuid().toString(), json, mail.getIdentifier());
        }).exceptionally(err -> {
            MailMe.debug(err);
            return false;
        });
    }

    @Override
    public CompletableFuture<Boolean> savePlayerMail(UUID uuid, Mail mail) {
        return CompletableFuture.supplyAsync(() -> executeSQL("INSERT INTO PlayerMail(uuid, mail_uuid, ts, archived) values (?,?,?,?)", uuid.toString(), mail.getUuid().toString(), System.currentTimeMillis(), mail.isArchived()));
    }

    @Override
    public CompletableFuture<Boolean> setArchived(UUID uuid, Mail mail) {
        return CompletableFuture.supplyAsync(() -> {
            if (mail.getColId() == null) {
                MailMe.debug(MySQLDatabase.class, "Cannot update mail! ColId is null! (We don't know what to update!!!)");
                return false;
            }
            return executeSQL("UPDATE PlayerMail SET archived = ? WHERE id = ?", mail.isArchived(), mail.getColId());
        });
    }

    @Override
    public CompletableFuture<Boolean> setRead(UUID uuid, Mail mail) {
        return CompletableFuture.supplyAsync(() -> {
            if (mail.getColId() == null) {
                MailMe.debug(MySQLDatabase.class, "Cannot update mail! ColId is null! (We don't know what to update!!!)");
                return false;
            }
            return executeSQL("UPDATE PlayerMail SET mail_read = true WHERE id = ?", mail.getColId());
        });
    }

    @Override
    public CompletableFuture<Mail> getPresetMail(String presetName) {
        return CompletableFuture.supplyAsync(() -> {
            Mail[] mail = getPresetMailFromTableQuery("SELECT * FROM Mail WHERE identifier_name='" + presetName + "'");
            if (mail.length == 0) {
                return null;
            }
            return mail[0];
        });
    }

    @Override
    public CompletableFuture<Boolean> deletePresetMail(String presetName) {
        return CompletableFuture.supplyAsync(() -> executeSQL("DELETE FROM Mail WHERE identifier_name = ?", presetName));
    }

    @Override
    public CompletableFuture<?> deletePlayerMail(UUID uuid, Mail[] mail) {
        return CompletableFuture.runAsync(() -> {
            for (Mail m : mail) {
                executeSQL("DELETE FROM PlayerMail WHERE id = ?", m.getColId());
            }
        });
    }

    @Override
    public void deletePlayerMail(UUID uuid, Mail mail) {
        CompletableFuture.runAsync(() -> executeSQL("DELETE FROM PlayerMail WHERE id = ?", mail.getColId()));
    }

    private boolean isExpired(Mail mail, long timeMillis) {
        boolean expired = timeMillis + mail.getExpiryTimeMilliSeconds() < System.currentTimeMillis();
        if (expired && !mail.isArchived()) {
            CompletableFuture.runAsync(() -> executeSQL("DELETE FROM PlayerMail WHERE id = ?", mail.getColId()));
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
            stmt.execute("create table if not exists PlayerMail(id int auto_increment primary key, uuid varchar(36) not null, mail_uuid varchar(36) not null, mail_read tinyint(1) not null default false, ts bigint not null, archived boolean not null default false)");
            stmt.execute("create table if not exists Mail(mail_uuid varchar(36) not null, mail_obj mediumtext not null, identifier_name varchar(36))");
            applyMissingCols(stmt);
        } catch (SQLException e) {
            MailMe.debug(MySQLDatabase.class, "Error occurred while asserting table readiness. Error code: " + e.getErrorCode() + ", " + e.getMessage());
            MailMe.debug(e);
        }
    }

    // Used when columns are added
    private void applyMissingCols(Statement stmt) {
        try {
            stmt.execute("ALTER TABLE PlayerMail ADD archived boolean not null default false");
        } catch (Exception e) {
            // Typically we don't care if an exception occurs. Usually means already exists and just a way of checking it exists without restricting sql versions
            MailMe.debug(MySQLDatabase.class, "THIS MAY NOT BE AN ERROR! DO NOT REPORT THIS TO DEVELOPER UNLESS ASKED!");
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
            MailMe.debug(MySQLDatabase.class, "Failed to load driver \"" + settings.getDriver() + "\" when loading the SQLDatabase");
            MailMe.debug(e);
            return false;
        }
        return true;
    }
    private void closeStatement(PreparedStatement statement) {
        try {
            if (statement != null && !statement.isClosed())
                statement.close();
        } catch (SQLException throwables) {
            MailMe.debug(MySQLDatabase.class, "------------");
            Bukkit.getLogger().log(Level.WARNING, "Could not close prepared statement. This may cause backend issues in future.");
            MailMe.debug(MySQLDatabase.class, String.format("SQL State: %s | Error Msg: %s | Error Code: %s", throwables.getSQLState(), throwables.getMessage(), throwables.getErrorCode()));
        }
    }

    /*
        Executes SQL with values provided
        True if rows manipulated returned are greater than 0 (success)
        False if rows manipulated returned are less than 0 (fail)
     */
    private boolean executeSQL(String query, Object... values) {
        MailMe.debug(SQLException.class, "Executing SQL Query: " + query + " with values " + Arrays.toString(values));

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(query);
            setValues(ps, values);
            return ps.executeUpdate() > 0;

        } catch (SQLException exception) {
            MailMe.debug(MySQLDatabase.class, "------- \n" +
                    "Exception occured when executing query| " + query + " | with values| " + Arrays.toString(values) + " |");
            MailMe.debug(exception);
        } finally {
            closeStatement(ps);
        }
        return false;
    }

    private static void setValues(PreparedStatement preparedStatement, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
    }

    private Mail[] getPresetMailFromTableQuery(String query) {
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs == null) {
                MailMe.debug(MySQLDatabase.class, "Result was null (mailtablequery)");
                return new Mail[0];
            }

            List<Mail> mailList = new ArrayList<>();

            while (rs.next()) {
                Mail mailObj = MailMe.getInstance().getCache().getFileUtil().deserializeMail(rs.getString("mail_obj"));
                mailList.add(mailObj);
            }

            return mailList.toArray(new Mail[0]);

        } catch (SQLException ex) {
            MailMe.debug(MySQLDatabase.class, ex.getMessage());
            return new Mail[0];
        }
    }

    public CompletableFuture<Mail[]> getMailFromQuery(String query) {
        return CompletableFuture.supplyAsync(() -> {
            List<Mail> mail = new ArrayList<>();
            try {
                PreparedStatement ps = connection.prepareStatement(query);
                ResultSet playerMailRS = ps.executeQuery();
                if (playerMailRS == null) {
                    MailMe.debug(MySQLDatabase.class, "Result set was null!");
                    return new Mail[0];
                }
                // Loop through all instances of the player's uuid
                while (playerMailRS.next()) {
                    String mailId = playerMailRS.getString("mail_uuid");
                    PreparedStatement mailStmt = connection.prepareStatement("SELECT mail_obj FROM Mail WHERE mail_uuid = ?");
                    mailStmt.setString(1, mailId);
                    ResultSet mailRS = mailStmt.executeQuery();
                    // Grab from Mail table the uuid we found in our playermail table
                    if (mailRS.next()) {
                        // deserialize our object
                        Mail mailObj = MailMe.getInstance().getCache().getFileUtil().deserializeMail(mailRS.getString("mail_obj"));
                        mailObj.setRead(playerMailRS.getBoolean("mail_read"));
                        mailObj.setColId(playerMailRS.getInt("id"));
                        mailObj.setArchived(playerMailRS.getBoolean("archived"));
                        long ts = playerMailRS.getLong("ts");
                        mailObj.setDateReceived(ts);
                        // If archived or not expired (isExpired will auto delete it)
                        if (mailObj.isArchived() || !isExpired(mailObj, ts))
                            mail.add(mailObj);

                    } else {
                        MailMe.debug(MySQLDatabase.class, "Tried to access mail object " + mailId + " in table but it does not exist!");
                    }
                }
                Collections.reverse(mail);
            } catch (SQLException e) {
                MailMe.debug(MySQLDatabase.class, "Error occurred while retrieving data. Error code: " + e.getErrorCode() + ", " + e.getMessage());
            } finally {

            }
            return mail.toArray(new Mail[0]);
        }).exceptionally(e -> {
            MailMe.debug(e);
            return new Mail[0];
        });

    }
}