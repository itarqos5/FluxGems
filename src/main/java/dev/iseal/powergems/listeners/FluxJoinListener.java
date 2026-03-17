package dev.iseal.powergems.listeners;

import dev.iseal.powergems.PowerGems;
import dev.iseal.powergems.managers.GemManager;
import dev.iseal.powergems.managers.SingletonManager;
import dev.iseal.powergems.managers.database.FluxDataManager;
import dev.iseal.powergems.managers.database.FluxPlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class FluxJoinListener implements Listener {

    private final GemManager gemManager = SingletonManager.getInstance().gemManager;
    private final FluxDataManager fluxDataManager = SingletonManager.getInstance().fluxDataManager;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        refreshExistingFluxLores(player);
        if (hasAnyFluxInInventory(player)) {
            return;
        }

        fluxDataManager.getPlayerData(player.getUniqueId()).thenAccept(data -> {
            PowerGems.getPlugin().getServer().getScheduler().runTask(PowerGems.getPlugin(), () -> {
                if (!player.isOnline() || hasAnyFluxInInventory(player)) {
                    return;
                }

                if (data.hasFlux()) {
                    restoreFluxFromDatabase(player, data);
                    return;
                }

                assignAndGiveRandomJoinFlux(player);
            });
        }).exceptionally(ex -> {
            PowerGems.getPlugin().getLogger().warning("Failed to fetch fluxdata for " + player.getName() + ": " + ex.getMessage());
            return null;
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        fluxDataManager.invalidate(event.getPlayer().getUniqueId());
    }

    private void restoreFluxFromDatabase(Player player, FluxPlayerData data) {
        if (GemManager.lookUpID(data.getFlux()) < 0) {
            PowerGems.getPlugin().getLogger().warning(
                    "Cannot restore unknown flux for " + player.getName() + " (flux=" + data.getFlux() + ")"
            );
            return;
        }

        int fluxLevel = Math.max(1, data.getFluxLevel());
        ItemStack restoredFlux = gemManager.createGem(data.getFlux(), fluxLevel);
        if (restoredFlux == null || !gemManager.isGem(restoredFlux)) {
            PowerGems.getPlugin().getLogger().warning(
                    "Failed to restore saved flux for " + player.getName() + " (flux=" + data.getFlux() + ", level=" + fluxLevel + ")"
            );
            return;
        }

        moveOffhandItemSafely(player);
        player.getInventory().setItemInOffHand(restoredFlux);
    }

    private void assignAndGiveRandomJoinFlux(Player player) {
        ItemStack randomFlux = gemManager.createGem();
        if (randomFlux == null) {
            return;
        }

        String fluxName = gemManager.getName(randomFlux);
        int fluxLevel = gemManager.getLevel(randomFlux);
        fluxDataManager.assignJoinFlux(player.getUniqueId(), fluxName, fluxLevel).thenRun(() ->
                PowerGems.getPlugin().getServer().getScheduler().runTask(PowerGems.getPlugin(), () -> {
                    if (!player.isOnline() || hasAnyFluxInInventory(player)) {
                        return;
                    }
                    moveOffhandItemSafely(player);
                    player.getInventory().setItemInOffHand(randomFlux);
                })
        ).exceptionally(ex -> {
            PowerGems.getPlugin().getLogger().warning("Failed to sync join flux for " + player.getName() + ": " + ex.getMessage());
            return null;
        });
    }

    private boolean hasAnyFluxInInventory(Player player) {
        if (gemManager.isGem(player.getInventory().getItemInOffHand())) {
            return true;
        }
        for (ItemStack stack : player.getInventory().getContents()) {
            if (gemManager.isGem(stack)) {
                return true;
            }
        }
        return false;
    }

    private void refreshExistingFluxLores(Player player) {
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (gemManager.isGem(offhand)) {
            gemManager.attemptFixGem(offhand);
            player.getInventory().setItemInOffHand(offhand);
        }

        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (!gemManager.isGem(stack)) {
                continue;
            }
            gemManager.attemptFixGem(stack);
            player.getInventory().setItem(i, stack);
        }
    }

    private void moveOffhandItemSafely(Player player) {
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand == null || offhand.getType() == Material.AIR) {
            return;
        }
        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(offhand);
            return;
        }
        player.getWorld().dropItemNaturally(player.getLocation(), offhand);
    }
}
