package me.mrdeej.mysql;

import me.mrdeej.common.database.SqlCredentials;
import me.mrdeej.common.database.SyncDatabase;
import org.bukkit.plugin.java.JavaPlugin;

public final class MySQL extends JavaPlugin {

    private SyncDatabase database;

    @Override
    public void onEnable() {
        // Plugin startup logic

        database = new SyncDatabase(constructDatabaseCredentials());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private SqlCredentials constructDatabaseCredentials() {
        return new SqlCredentials(
                getConfig().getString("database.host"),
                getConfig().getInt("database.port"),
                getConfig().getString("database.database"),
                getConfig().getString("database.username"),
                getConfig().getString("database.password")
        );
    }

    public SyncDatabase getSqlDatabase() {
        return database;
    }
}
