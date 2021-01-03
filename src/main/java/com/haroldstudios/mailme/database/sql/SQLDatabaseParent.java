package com.haroldstudios.mailme.database.sql;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.DatabaseConnector;
import com.haroldstudios.mailme.database.DatabaseSettingsImpl;
import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.mail.Mail;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public abstract class SQLDatabaseParent implements DatabaseConnector, PlayerMailDAO {

    protected final Logger logger;
    protected Connection connection = null;
    protected final DatabaseSettingsImpl settings;
    private final String connectionUrl;

    public SQLDatabaseParent(DatabaseSettingsImpl settings, String connectionUrl) {
        this.settings = settings;
        logger = Logger.getAnonymousLogger();
        this.connectionUrl = connectionUrl;
    }

    @Override
    public boolean connect() {
        if (isConnected()) return true;
        if (!loadDriver()) return false;

        logger.info(String.format("Beginning connection to SQL server at host: %s, port: %s, database_name: %s, using SSL: %s", settings.getHost(), settings.getPort(), settings.getDatabaseName(), settings.useSSL()));
        try {
            connection = DriverManager.getConnection(connectionUrl, settings.getUsername(), settings.getPassword());
            assertTableReady();
        } catch (SQLException e) {
            logger.severe("Could not connect to the database! " + e.getMessage());
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
                ResultSet resultSet = statement.executeQuery("select mail_uuid from PlayerMail where uuid='" + uuid.toString() + "' and read=false");

                int fetchSize = resultSet.getFetchSize();

                statement.close(); // Auto closes resultset when statement is closed

                return fetchSize > 1;

            } catch (SQLException e) {
                logger.severe("Error occurred while retrieving data for " + uuid.toString() + ". Error code: " + e.getErrorCode() + ", " + e.getMessage());
            }
            return false;
        });
    }

    @Override
    public CompletableFuture<Mail[]> getUnreadMail(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select mail_uuid from PlayerMail where uuid='" + uuid.toString() + "' and 'read'=false");

                // Define our array size
                Mail[] mail = new Mail[resultSet.getFetchSize() - 1];

                // Loop through all instances of the player's uuid
                while (resultSet.next()) {
                    String mailId = resultSet.getString("mail_uuid");
                    Statement mailStmt = connection.createStatement();
                    ResultSet mailRS = mailStmt.executeQuery("select mail_obj from Mail where mail_uuid='" + mailId + "'");

                    // Grab from Mail table the uuid we found in our playermail table
                    if (mailRS.first()) {
                        // deserialize our object
                        mail[resultSet.getRow() - 1] = MailMe.getInstance().getCache().getFileUtil().deserialize(Mail.class, mailRS.getString("mail_obj"));
                    } else {
                        logger.warning("User: " + uuid + " tried to access mail object " + mailId + "in table but it does not exist!");
                    }
                }
                return mail;
            } catch (SQLException e) {
                logger.severe("Error occurred while retrieving data for " + uuid.toString() + ". Error code: " + e.getErrorCode() + ", " + e.getMessage());
            }
            return null;
        });
    }

    @Override
    public void saveMailObj(Mail mail) {
        CompletableFuture.runAsync(() -> {

            Statement statement;
            try {
                statement = connection.createStatement();
                String json = MailMe.getInstance().getCache().getFileUtil().serialize(mail);
                statement.execute("insert into Mail values ('"+mail.getUuid()+"', '"+json+"')");

                statement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    @Override
    public void savePlayerMail(UUID uuid, Mail mail) {
        CompletableFuture.runAsync(() -> {
            try {
                Statement statement = connection.createStatement();
                statement.execute("insert into PlayerMail(uuid, mail_uuid) values ('"+uuid+"', '"+mail.getUuid()+"')");

                statement.close();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        });
    }

    /**
     * Creates any missing tables and columns
     */
    public void assertTableReady() {

        try {
            Statement stmt = connection.createStatement();
            // Create Tables if not already there
            stmt.execute("create table if not exists PlayerMail(uuid varchar(36) not null, mail_uuid varchar(36) not null, mail_read tinyint(1) not null default false, ts timestamp default current_timestamp)");
            stmt.execute("create table if not exists Mail(mail_uuid varchar(36) not null, mail_obj mediumtext not null)");
            //todo eventually add column checker for playermail

        } catch (SQLException e) {
            logger.severe("Error occurred while asserting table readiness. Error code: " + e.getErrorCode() + ", " + e.getMessage());
        }
    }

    /**
     * Attempts to load SQL driver
     *
     * @return If Successful
     */
    public boolean loadDriver() {
        try {
            Class.forName(settings.getDriver());
        } catch (Exception e) {
            logger.severe("Failed to load driver \"" + settings.getDriver() + "\" when loading the MySQLDatabase");
            return false;
        }
        return true;
    }
}
