package dev.iseal.powergems.managers.Configuration;

import dev.iseal.powergems.PowerGems;
import dev.iseal.powergems.managers.ConfigManager;
import dev.iseal.powergems.misc.AbstractClasses.AbstractConfigManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConfigManager extends AbstractConfigManager {

    public DatabaseConfigManager() {
        super("database");
    }

    @Override
    public void setUpConfig() {
        file.setDefault("type", "sqlite");
        file.setDefault("sqlite.file", "powergems.db");
        file.setDefault("pool.maximumPoolSize", 16);
        file.setDefault("pool.minimumIdle", 4);
        file.setDefault("pool.connectionTimeoutMs", 5000L);
        file.setDefault("mysql.host", "127.0.0.1");
        file.setDefault("mysql.port", 3306);
        file.setDefault("mysql.database", "powergems");
        file.setDefault("mysql.username", "root");
        file.setDefault("mysql.password", "change-me");
        file.setDefault("mysql.ssl", false);
        writeCommentedDatabaseConfigFile();
    }

    @Override
    public void lateInit() {
    }

    public String getType() {
        return file.getString("type").toLowerCase();
    }

    public String getSqliteFile() {
        return file.getString("sqlite.file");
    }

    public int getMaximumPoolSize() {
        return file.getInt("pool.maximumPoolSize");
    }

    public int getMinimumIdle() {
        return file.getInt("pool.minimumIdle");
    }

    public long getConnectionTimeoutMs() {
        return file.getLong("pool.connectionTimeoutMs");
    }

    public String getMysqlHost() {
        return file.getString("mysql.host");
    }

    public int getMysqlPort() {
        return file.getInt("mysql.port");
    }

    public String getMysqlDatabase() {
        return file.getString("mysql.database");
    }

    public String getMysqlUsername() {
        return file.getString("mysql.username");
    }

    public String getMysqlPassword() {
        return file.getString("mysql.password");
    }

    public boolean useMysqlSsl() {
        return file.getBoolean("mysql.ssl");
    }

    private void writeCommentedDatabaseConfigFile() {
        Path path = Path.of(ConfigManager.getConfigFolderPath(), "database.yml");
        if (Files.exists(path)) {
            try {
                try (var lines = Files.lines(path, StandardCharsets.UTF_8)) {
                    if (lines.anyMatch(line -> line.contains("# PowerGems database configuration"))) {
                        return;
                    }
                }
            } catch (IOException ignored) {
                // If we fail to read, we attempt to rewrite safely below.
            }
        }
        String ls = System.lineSeparator();
        String content =
                "# PowerGems database configuration" + ls +
                "# type: sqlite | mysql" + ls +
                "type: \"" + getType() + "\"" + ls +
                ls +
                "# SQLite settings (used when type=sqlite)" + ls +
                "sqlite:" + ls +
                "  file: \"" + getSqliteFile() + "\"" + ls +
                ls +
                "# Connection pool tuning for high player counts." + ls +
                "pool:" + ls +
                "  maximumPoolSize: " + getMaximumPoolSize() + ls +
                "  minimumIdle: " + getMinimumIdle() + ls +
                "  connectionTimeoutMs: " + getConnectionTimeoutMs() + ls +
                ls +
                "# MySQL settings (used when type=mysql)" + ls +
                "mysql:" + ls +
                "  host: \"" + getMysqlHost() + "\"" + ls +
                "  port: " + getMysqlPort() + ls +
                "  database: \"" + getMysqlDatabase() + "\"" + ls +
                "  username: \"" + getMysqlUsername() + "\"" + ls +
                "  password: \"" + getMysqlPassword() + "\"" + ls +
                "  ssl: " + useMysqlSsl() + ls;
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            PowerGems.getPlugin().getLogger().warning("Failed to write commented database.yml: " + e.getMessage());
        }
    }
}
