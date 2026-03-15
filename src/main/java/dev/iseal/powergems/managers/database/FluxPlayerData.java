package dev.iseal.powergems.managers.database;

import java.util.UUID;

public class FluxPlayerData {

    private final UUID uuid;
    private final String flux;
    private final int fluxLevel;
    private final boolean hasFlux;
    private final long lastSelectFlux;
    private final long createdAt;
    private final long updatedAt;

    public FluxPlayerData(UUID uuid, String flux, int fluxLevel, boolean hasFlux, long lastSelectFlux, long createdAt, long updatedAt) {
        this.uuid = uuid;
        this.flux = flux;
        this.fluxLevel = fluxLevel;
        this.hasFlux = hasFlux;
        this.lastSelectFlux = lastSelectFlux;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getFlux() {
        return flux;
    }

    public int getFluxLevel() {
        return fluxLevel;
    }

    public boolean hasFlux() {
        return hasFlux;
    }

    public long getLastSelectFlux() {
        return lastSelectFlux;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }
}
