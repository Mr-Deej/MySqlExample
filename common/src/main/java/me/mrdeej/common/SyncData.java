package me.mrdeej.common;

import me.mrdeej.common.database.SqlFunction;

import java.sql.ResultSet;
import java.time.Instant;

public class SyncData {

    public static SqlFunction<ResultSet, SyncData> transformer() {
        return results -> results.next() ? new SyncData(
                results.getString("discord_id"),
                results.getBoolean("boosting"),
                results.getLong("last_boost_reward"),
                results.getBoolean("one_time_reward")
        ) : null;
    }

    private final String discordId;
    private final boolean boosting;
    private Instant lastBoostReward;
    private boolean oneTimeReward;

    public SyncData(String discordId, boolean boosting, long lastBoostReward, boolean oneTimeReward)
    {
        this.discordId = discordId;
        this.boosting = boosting;
        this.lastBoostReward = Instant.ofEpochMilli(lastBoostReward);
        this.oneTimeReward = oneTimeReward;
    }

    public String getDiscordId()
    {
        return discordId;
    }

    public boolean isBoosting()
    {
        return boosting;
    }

    public Instant getLastBoostReward()
    {
        return lastBoostReward;
    }

    public void updateLastBoostReward()
    {
        lastBoostReward = Instant.now();
    }

    public boolean getOneTimeReward()
    {
        return oneTimeReward;
    }

    public void updateOneTimeReward()
    {
        oneTimeReward = true;
    }
}
