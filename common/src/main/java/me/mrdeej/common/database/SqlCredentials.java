package me.mrdeej.common.database;

public class SqlCredentials {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public SqlCredentials(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    String getHost() {
        return host;
    }

    int getPort() {
        return port;
    }

    String getDatabase() {
        return database;
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }
}