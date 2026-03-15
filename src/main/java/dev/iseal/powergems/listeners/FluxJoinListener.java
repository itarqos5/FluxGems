package dev.iseal.powergems.listeners;

import dev.iseal.powergems.PowerGems;
import dev.iseal.powergems.managers.GemManager;
import dev.iseal.powergems.managers.SingletonManager;
import dev.iseal.powergems.managers.database.FluxDataManager;
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
        ItemStack randomFlux = gemManager.createGem();
        if (randomFlux == null) {
            return;
        }

        moveOffhandItemSafely(player);
        player.getInventory().setItemInOffHand(randomFlux);

        String fluxName = gemManager.getName(randomFlux);
        int fluxLevel = gemManager.getLevel(randomFlux);
        fluxDataManager.assignJoinFlux(player.getUniqueId(), fluxName, fluxLevel).exceptionally(ex -> {
            PowerGems.getPlugin().getLogger().warning("Failed to sync join flux for " + player.getName() + ": " + ex.getMessage());
            return null;
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        fluxDataManager.invalidate(event.getPlayer().getUniqueId());
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
