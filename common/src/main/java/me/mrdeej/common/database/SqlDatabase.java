package me.mrdeej.common.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class SqlDatabase {
    protected final ExecutorService executor;

    SqlDatabase(SqlCredentials credentials, ExecutorService executor)
    {
        this.executor = executor;
        connect(credentials);
    }

    private void connect(SqlCredentials credentials)
    {
        HikariConfig config = new HikariConfig();
        config.setPoolName("boostsync");

        config.setJdbcUrl(String.format(
                "jdbc:mysql://%s:%s/%s",
                credentials.getHost(), credentials.getPort(), credentials.getDatabase())
        );

        config.setUsername(credentials.getUsername());
        config.setPassword(credentials.getPassword());

        config.addDataSourceProperty("cachePrepStmts", true);

        connectionPool = new HikariDataSource(config);
    }

    private HikariDataSource connectionPool;

    void executeUpdate(String statement)
    {
        executeUpdate(statement, SqlConsumer.blank());
    }

    void executeUpdate(String statement, SqlConsumer<PreparedStatement> consumer)
    {
        withConnection(connection ->
        {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            consumer.accept(preparedStatement);

            preparedStatement.execute();
        });
    }

    void asyncUpdate(String statement, SqlConsumer<PreparedStatement> consumer)
    {
        executor.submit(() -> executeUpdate(statement, consumer));
    }

    <T> T executeQuery(String statement, SqlConsumer<PreparedStatement> consumer, SqlFunction<ResultSet, T> function)
    {
        return withReturningConnection(connection ->
        {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            consumer.accept(preparedStatement);

            ResultSet results = preparedStatement.executeQuery();
            return function.apply(results);
        });
    }

    <T> CompletableFuture<T> asyncQuery(String statement, SqlConsumer<PreparedStatement> consumer, SqlFunction<ResultSet, T> function)
    {
        return CompletableFuture.supplyAsync(() -> executeQuery(statement, consumer, function), executor);
    }

    private void withConnection(SqlConsumer<Connection> consumer)
    {
        try (Connection connection = connectionPool.getConnection())
        {
            consumer.accept(connection);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    private <T> T withReturningConnection(SqlFunction<Connection, T> function)
    {
        try (Connection connection = connectionPool.getConnection())
        {
            return function.apply(connection);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
}