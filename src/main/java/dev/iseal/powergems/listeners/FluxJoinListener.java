package dev.iseal.powergems.listeners;

import dev.iseal.powergems.PowerGems;
import dev.iseal.powergems.managers.GemManager;
import dev.iseal.powergems.managers.SingletonManager;
import dev.iseal.powergems.managers.database.FluxDataManager;
import dev.iseal.powergems.managers.database.FluxPlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class FluxJoinListener implements Listener {

    private final GemManager gemManager = SingletonManager.getInstance().gemManager;
    private final FluxDataManager fluxDataManager = SingletonManager.getInstance().fluxDataManager;
    private final Logger logger = PowerGems.getPlugin().getLogger();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        debug("Join handler fired for " + player.getName());
        refreshExistingFluxLores(player);
        if (hasAnyFluxInInventory(player)) {
            debug("Player " + player.getName() + " already has a recognized flux after refresh.");
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
        refreshSlot(player, "offhand", -1, offhand);

        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            refreshSlot(player, "inventory", i, contents[i]);
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

    private void refreshSlot(Player player, String container, int slot, ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) {
            return;
        }

        boolean isGem = gemManager.isGem(stack);
        boolean isLikelyGem = gemManager.isLikelyGem(stack);
        debug("Inspecting " + container + " slot " + slot + " for " + player.getName()
                + " | isGem=" + isGem
                + " | isLikelyGem=" + isLikelyGem
                + " | item=" + describeItem(stack));

        if (!isLikelyGem) {
            return;
        }

        ItemStack rebuilt = gemManager.rebuildGem(stack);
        debug("Rebuilt " + container + " slot " + slot + " for " + player.getName()
                + " | before=" + describeItem(stack)
                + " | after=" + describeItem(rebuilt));

        if ("offhand".equals(container)) {
            player.getInventory().setItemInOffHand(rebuilt);
            return;
        }
        player.getInventory().setItem(slot, rebuilt);
    }

    private String describeItem(ItemStack item) {
        if (item == null) {
            return "null";
        }

        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
        String displayName = meta != null && meta.hasDisplayName() ? ChatColor.stripColor(meta.getDisplayName()) : "";
        List<String> lore = meta != null && meta.hasLore() ? meta.getLore() : null;
        String firstLore = lore != null && !lore.isEmpty() ? ChatColor.stripColor(Objects.requireNonNullElse(lore.get(0), "")) : "";
        String secondLore = lore != null && lore.size() > 1 ? ChatColor.stripColor(Objects.requireNonNullElse(lore.get(1), "")) : "";
        String thirdLore = lore != null && lore.size() > 2 ? ChatColor.stripColor(Objects.requireNonNullElse(lore.get(2), "")) : "";

        return "type=" + item.getType()
                + ", amount=" + item.getAmount()
                + ", name=" + displayName
                + ", firstLore=" + firstLore
                + ", secondLore=" + secondLore
                + ", thirdLore=" + thirdLore;
    }

    private void debug(String message) {
        logger.warning("[FluxJoinDebug] " + message);
    }
}
