package dev.iseal.powergems.managers.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.iseal.powergems.PowerGems;
import dev.iseal.powergems.managers.Configuration.DatabaseConfigManager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqlFluxDatabase {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS fluxdata (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "flux VARCHAR(32) NOT NULL," +
                    "fluxlevel INTEGER NOT NULL DEFAULT 1," +
                    "hasFlux BOOLEAN NOT NULL DEFAULT FALSE," +
                    "last_selectflux BIGINT NOT NULL DEFAULT 0," +
                    "created_at BIGINT NOT NULL," +
                    "updated_at BIGINT NOT NULL" +
                    ");";

    private final DatabaseConfigManager dbConfig;
    private final Logger logger = PowerGems.getPlugin().getLogger();
    private HikariDataSource dataSource;
    private ExecutorService dbExecutor;
    private boolean mysql;

    public SqlFluxDatabase(DatabaseConfigManager dbConfig) {
        this.dbConfig = dbConfig;
    }

    public void init() {
        this.mysql = "mysql".equalsIgnoreCase(dbConfig.getType());

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(Math.max(4, dbConfig.getMaximumPoolSize()));
        config.setMinimumIdle(Math.max(2, dbConfig.getMinimumIdle()));
        config.setConnectionTimeout(Math.max(2000L, dbConfig.getConnectionTimeoutMs()));
        config.setPoolName("PowerGems-FluxPool");

        if (mysql) {
            String url = "jdbc:mysql://" + dbConfig.getMysqlHost() + ":" + dbConfig.getMysqlPort() + "/" + dbConfig.getMysqlDatabase() +
                    "?useSSL=" + dbConfig.useMysqlSsl() + "&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            config.setJdbcUrl(url);
            config.setUsername(dbConfig.getMysqlUsername());
            config.setPassword(dbConfig.getMysqlPassword());
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            File dbFile = new File(PowerGems.getPlugin().getDataFolder(), dbConfig.getSqliteFile());
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            config.setJdbcUrl(url);
            config.setDriverClassName("org.sqlite.JDBC");
        }

        this.dataSource = new HikariDataSource(config);
        this.dbExecutor = Executors.newFixedThreadPool(Math.max(2, config.getMaximumPoolSize() / 2));
        createTable();
    }

    private void createTable() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
            logger.info("Initialized fluxdata table.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create fluxdata table", e);
        }
    }

    public CompletableFuture<FluxPlayerData> getOrCreate(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT uuid, flux, fluxlevel, hasFlux, last_selectflux, created_at, updated_at FROM fluxdata WHERE uuid = ?";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid.toString());
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to fetch fluxdata for " + uuid, e);
            }

            long now = System.currentTimeMillis();
            FluxPlayerData defaults = new FluxPlayerData(uuid, "", 1, false, 0L, now, now);
            saveBlocking(defaults);
            return defaults;
        }, dbExecutor);
    }

    public CompletableFuture<Void> save(FluxPlayerData data) {
        return CompletableFuture.runAsync(() -> {
            String sql;
            if (mysql) {
                sql = "INSERT INTO fluxdata (uuid, flux, fluxlevel, hasFlux, last_selectflux, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE flux=VALUES(flux), fluxlevel=VALUES(fluxlevel), hasFlux=VALUES(hasFlux), " +
                        "last_selectflux=VALUES(last_selectflux), created_at=VALUES(created_at), updated_at=VALUES(updated_at)";
            } else {
                sql = "INSERT INTO fluxdata (uuid, flux, fluxlevel, hasFlux, last_selectflux, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET flux=excluded.flux, fluxlevel=excluded.fluxlevel, hasFlux=excluded.hasFlux, " +
                        "last_selectflux=excluded.last_selectflux, created_at=excluded.created_at, updated_at=excluded.updated_at";
            }
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, data.getUuid().toString());
                statement.setString(2, data.getFlux());
                statement.setInt(3, data.getFluxLevel());
                statement.setBoolean(4, data.hasFlux());
                statement.setLong(5, data.getLastSelectFlux());
                statement.setLong(6, data.getCreatedAt());
                statement.setLong(7, data.getUpdatedAt());
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to save fluxdata for " + data.getUuid(), e);
            }
        }, dbExecutor);
    }

    private FluxPlayerData mapRow(ResultSet rs) throws SQLException {
        return new FluxPlayerData(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("flux"),
                rs.getInt("fluxlevel"),
                rs.getBoolean("hasFlux"),
                rs.getLong("last_selectflux"),
                rs.getLong("created_at"),
                rs.getLong("updated_at")
        );
    }

    private void saveBlocking(FluxPlayerData data) {
        String sql;
        if (mysql) {
            sql = "INSERT INTO fluxdata (uuid, flux, fluxlevel, hasFlux, last_selectflux, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE flux=VALUES(flux), fluxlevel=VALUES(fluxlevel), hasFlux=VALUES(hasFlux), " +
                    "last_selectflux=VALUES(last_selectflux), created_at=VALUES(created_at), updated_at=VALUES(updated_at)";
        } else {
            sql = "INSERT INTO fluxdata (uuid, flux, fluxlevel, hasFlux, last_selectflux, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET flux=excluded.flux, fluxlevel=excluded.fluxlevel, hasFlux=excluded.hasFlux, " +
                    "last_selectflux=excluded.last_selectflux, created_at=excluded.created_at, updated_at=excluded.updated_at";
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, data.getUuid().toString());
            statement.setString(2, data.getFlux());
            statement.setInt(3, data.getFluxLevel());
            statement.setBoolean(4, data.hasFlux());
            statement.setLong(5, data.getLastSelectFlux());
            statement.setLong(6, data.getCreatedAt());
            statement.setLong(7, data.getUpdatedAt());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to save fluxdata for " + data.getUuid(), e);
        }
    }

    public void shutdown() {
        if (dbExecutor != null) {
            dbExecutor.shutdown();
        }
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
