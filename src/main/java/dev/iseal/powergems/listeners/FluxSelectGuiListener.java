package dev.iseal.powergems.listeners;

import dev.iseal.powergems.PowerGems;
import dev.iseal.powergems.managers.GemManager;
import dev.iseal.powergems.managers.SingletonManager;
import dev.iseal.powergems.managers.database.FluxDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FluxSelectGuiListener implements Listener {

    public static final String GUI_TITLE = ChatColor.DARK_AQUA + "Select Your Flux";
    private static final int[] FLUX_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    public void openFor(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 45, GUI_TITLE);
        ItemStack background = createItem(Material.CYAN_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, background);
        }

        ItemStack header = createItem(Material.NETHER_STAR, ChatColor.AQUA + "Flux Selector");
        ItemMeta headerMeta = header.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Choose your desired Flux.");
        lore.add(ChatColor.GRAY + "Cooldown: " + ChatColor.GOLD + "24h");
        headerMeta.setLore(lore);
        headerMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        header.setItemMeta(headerMeta);
        inventory.setItem(4, header);

        GemManager gemManager = gemManager();
        List<String> names = new ArrayList<>(gemManager.getGems().keySet());
        names.sort(Comparator.naturalOrder());
        for (int i = 0; i < names.size() && i < FLUX_SLOTS.length; i++) {
            String gemName = names.get(i);
            ItemStack flux = gemManager.createGem(gemName, 1);
            if (flux == null) {
                continue;
            }
            ItemMeta meta = flux.getItemMeta();
            List<String> itemLore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
            itemLore.add("");
            itemLore.add(ChatColor.GREEN + "Click to select this Flux");
            meta.setLore(itemLore);
            flux.setItemMeta(meta);
            inventory.setItem(FLUX_SLOTS[i], flux);
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onSelect(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!GUI_TITLE.equals(event.getView().getTitle())) {
            return;
        }
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        GemManager gemManager = gemManager();
        FluxDataManager fluxDataManager = fluxDataManager();
        if (clicked == null || !gemManager.isGem(clicked)) {
            return;
        }

        String selectedFlux = gemManager.getName(clicked);
        fluxDataManager.trySelectFlux(player.getUniqueId(), selectedFlux, 1).thenAccept(remaining -> {
            Bukkit.getScheduler().runTask(PowerGems.getPlugin(), () -> {
                if (remaining > 0L) {
                    long hours = TimeUnit.MILLISECONDS.toHours(remaining);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60;
                    player.sendMessage(ChatColor.RED + "You can use /selectflux again in " + hours + "h " + minutes + "m.");
                    return;
                }

                ItemStack newFlux = gemManager.createGem(selectedFlux, 1);
                if (newFlux == null) {
                    player.sendMessage(ChatColor.RED + "Failed to create that Flux.");
                    return;
                }

                ItemStack offhand = player.getInventory().getItemInOffHand();
                if (offhand != null && offhand.getType() != Material.AIR) {
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(offhand);
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), offhand);
                    }
                }

                player.getInventory().setItemInOffHand(newFlux);
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Selected Flux: " + ChatColor.AQUA + selectedFlux + " Flux");
            });
        });
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private GemManager gemManager() {
        return SingletonManager.getInstance().gemManager;
    }

    private FluxDataManager fluxDataManager() {
        return SingletonManager.getInstance().fluxDataManager;
    }
}
