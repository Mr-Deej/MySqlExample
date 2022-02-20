package me.mrdeej.common.database;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlFunction<T, R> {
    R apply(T object) throws SQLException;
}
