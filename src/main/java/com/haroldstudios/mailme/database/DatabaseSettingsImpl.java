package com.haroldstudios.mailme.database;

public class DatabaseSettingsImpl {

    private String host;
    private int port;
    private String databaseName;
    private String username;
    private String password;
    private boolean useSSL;
    private String driver;

    /**
     * Hosts database settings
     * @param host - database host
     * @param port - port
     * @param databaseName - database name
     * @param username - username
     * @param password - password
     * @param driver - Driver to use
     */
    public DatabaseSettingsImpl(String host, int port, String databaseName, String username, String password, boolean useSSL, String driver) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.useSSL = useSSL;
        this.driver = driver;
    }

    public boolean useSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean bool) {
        useSSL = bool;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUsername() {
        return username;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
