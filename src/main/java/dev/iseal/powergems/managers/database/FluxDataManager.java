package dev.iseal.powergems.managers.database;

import dev.iseal.powergems.managers.Configuration.DatabaseConfigManager;
import dev.iseal.powergems.managers.SingletonManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class FluxDataManager {

    public static final long SELECT_FLUX_COOLDOWN_MS = 24L * 60L * 60L * 1000L;

    private static FluxDataManager instance;

    public static FluxDataManager getInstance() {
        if (instance == null) {
            instance = new FluxDataManager();
        }
        return instance;
    }

    private final Map<UUID, FluxPlayerData> cache = new ConcurrentHashMap<>();
    private SqlFluxDatabase database;

    private FluxDataManager() {
    }

    public void init() {
        DatabaseConfigManager dbcm = SingletonManager.getInstance().configManager.getRegisteredConfigInstance(DatabaseConfigManager.class);
        this.database = new SqlFluxDatabase(dbcm);
        this.database.init();
    }

    public CompletableFuture<FluxPlayerData> getPlayerData(UUID uuid) {
        FluxPlayerData inCache = cache.get(uuid);
        if (inCache != null) {
            return CompletableFuture.completedFuture(inCache);
        }
        return database.getOrCreate(uuid).thenApply(data -> {
            cache.put(uuid, data);
            return data;
        });
    }

    public CompletableFuture<Void> assignJoinFlux(UUID uuid, String flux, int level) {
        long now = System.currentTimeMillis();
        return getPlayerData(uuid).thenCompose(existing -> {
            FluxPlayerData updated = new FluxPlayerData(
                    uuid,
                    flux,
                    Math.max(1, level),
                    true,
                    existing.getLastSelectFlux(),
                    existing.getCreatedAt() <= 0L ? now : existing.getCreatedAt(),
                    now
            );
            cache.put(uuid, updated);
            return database.save(updated);
        });
    }

    public CompletableFuture<Long> trySelectFlux(UUID uuid, String flux, int level) {
        long now = System.currentTimeMillis();
        return getPlayerData(uuid).thenCompose(existing -> {
            long elapsed = now - existing.getLastSelectFlux();
            if (existing.getLastSelectFlux() > 0L && elapsed < SELECT_FLUX_COOLDOWN_MS) {
                return CompletableFuture.completedFuture(SELECT_FLUX_COOLDOWN_MS - elapsed);
            }

            FluxPlayerData updated = new FluxPlayerData(
                    uuid,
                    flux,
                    Math.max(1, level),
                    true,
                    now,
                    existing.getCreatedAt() <= 0L ? now : existing.getCreatedAt(),
                    now
            );
            cache.put(uuid, updated);
            return database.save(updated).thenApply(v -> 0L);
        });
    }

    public long getRemainingSelectCooldown(FluxPlayerData data) {
        long elapsed = System.currentTimeMillis() - data.getLastSelectFlux();
        if (data.getLastSelectFlux() <= 0L || elapsed >= SELECT_FLUX_COOLDOWN_MS) {
            return 0L;
        }
        return SELECT_FLUX_COOLDOWN_MS - elapsed;
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    public void shutdown() {
        if (database != null) {
            database.shutdown();
        }
    }
}
