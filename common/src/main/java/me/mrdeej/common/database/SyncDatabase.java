package me.mrdeej.common.database;

import me.mrdeej.common.PendingData;
import me.mrdeej.common.SyncData;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class SyncDatabase extends SqlDatabase {
    public SyncDatabase(SqlCredentials credentials)
    {
        super(credentials, Executors.newCachedThreadPool(
                runnable -> new Thread(runnable, "boostsync-database-thread"))
        );

        createTables();
    }

    /* Pending syncs */
    public void insertCode(String code, Instant expiry, UUID uuid, String username)
    {
        asyncUpdate(
                "INSERT INTO boostsync_pending VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE code = ?, expiry = ?, username = ?;",
                statement ->
                {
                    statement.setString(1, code);
                    statement.setLong(2, expiry.toEpochMilli());
                    statement.setString(3, uuid.toString());
                    statement.setString(4, username);

                    statement.setString(5, code);
                    statement.setLong(6, expiry.toEpochMilli());
                    statement.setString(7, username);
                }
        );
    }

    public PendingData fetchPendingDataSync(String code)
    {
        return executeQuery(
                "SELECT * FROM boostsync_pending WHERE code = ?;",
                statement -> statement.setString(1, code),
                results -> results.next() ? new PendingData(
                        results.getString("code"),
                        results.getLong("expiry"),
                        results.getString("uuid"),
                        results.getString("username")
                ) : null
        );
    }

    public CompletableFuture<PendingData> fetchPendingData(String code)
    {
        return CompletableFuture.supplyAsync(
                () -> fetchPendingDataSync(code),
                executor
        );
    }

    public void removeCode(String code)
    {
        asyncUpdate(
                "DELETE FROM boostsync_pending WHERE code = ?;",
                statement -> statement.setString(1, code)
        );
    }

    /* Established syncs */
    public void insertSyncData(UUID uuid, SyncData data)
    {
        asyncUpdate(
                "INSERT INTO boostsync_synced VALUES (?,?,?,?);",
                statement ->
                {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, data.getDiscordId());
                    statement.setBoolean(3, data.isBoosting());
                    statement.setLong(4, data.getLastBoostReward().toEpochMilli());
                }
        );
    }

    public void updateBoostingStatus(String discordId, boolean boosting)
    {
        asyncUpdate(
                "UPDATE boostsync_synced SET boosting = ? WHERE discord_id = ?;",
                statement ->
                {
                    statement.setBoolean(1, boosting);
                    statement.setString(2, discordId);
                }
        );
    }

    public void updateOneTimeReward(UUID uuid)
    {
        asyncUpdate(
                "UPDATE boostsync_synced SET one_time_reward = true WHERE uuid = ?;",
                statement -> statement.setString(1, uuid.toString())
        );
    }

    public void updateLastBoostReward(UUID uuid, Instant instant)
    {
        asyncUpdate(
                "UPDATE boostsync_synced SET last_boost_reward = ? WHERE uuid = ?;",
                statement ->
                {
                    statement.setLong(1, instant.toEpochMilli());
                    statement.setString(2, uuid.toString());
                }
        );
    }

    public CompletableFuture<SyncData> fetchSyncData(UUID uuid)
    {
        return asyncQuery(
                "SELECT * FROM boostsync_synced WHERE uuid = ?;",
                statement -> statement.setString(1, uuid.toString()),
                SyncData.transformer()
        );
    }

    public CompletableFuture<SyncData> fetchSyncData(String discordId)
    {
        return asyncQuery(
                "SELECT * FROM boostsync_synced WHERE discord_id = ?;",
                statement -> statement.setString(1, discordId),
                SyncData.transformer()
        );
    }

    public void deleteSyncData(UUID uuid)
    {
        asyncUpdate(
                "DELETE FROM boostsync_synced WHERE uuid = ?;",
                statement -> statement.setString(1, uuid.toString())
        );
    }

    /* Tables */
    private void createTables()
    {
        executeUpdate(
                "CREATE TABLE IF NOT EXISTS `boostsync_pending` (" +
                        "`code` CHAR(5) UNIQUE KEY, " +
                        "`expiry` BIGINT NOT NULL, " +
                        "`uuid` CHAR(36) UNIQUE KEY NOT NULL, " +
                        "`username` VARCHAR(16) UNIQUE KEY NOT NULL" +
                        ");"
        );

        executeUpdate(
                "CREATE TABLE IF NOT EXISTS `boostsync_synced` (" +
                        "`uuid` CHAR(36) PRIMARY KEY, " +
                        "`discord_id` VARCHAR(20) UNIQUE KEY NOT NULL, " +
                        "`boosting` BOOLEAN NOT NULL, " +
                        "`last_boost_reward` BIGINT NOT NULL, " +
                        "`one_time_reward` BOOLEAN NOT NULL" +
                        ");"
        );
    }
}
