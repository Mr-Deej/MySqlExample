package me.mrdeej.common;

import java.time.Instant;
import java.util.UUID;

public class PendingData {

    private final String code;
    private final Instant expiry;
    private final UUID uuid;
    private final String username;

    public PendingData(String code, long expiry, String uuid, String username)
    {
        this.code = code;
        this.expiry = Instant.ofEpochMilli(expiry);
        this.uuid = UUID.fromString(uuid);
        this.username = username;
    }

    public String getCode()
    {
        return code;
    }

    public boolean hasExpired()
    {
        return Instant.now().isAfter(expiry) && expiry.toEpochMilli() != 0L;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public String getUsername()
    {
        return username;
    }

}
