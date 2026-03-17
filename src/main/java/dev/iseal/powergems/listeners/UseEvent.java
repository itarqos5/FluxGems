package dev.iseal.powergems.listeners;

import dev.iseal.powergems.managers.Configuration.ActiveGemsConfigManager;
import dev.iseal.powergems.managers.GemManager;
import dev.iseal.powergems.managers.SingletonManager;
import dev.iseal.powergems.managers.TempDataManager;
import dev.iseal.sealLib.Systems.I18N.I18N;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class UseEvent implements Listener {

    private final SingletonManager sm = SingletonManager.getInstance();
    private final TempDataManager tdm = sm.tempDataManager;
    private final ActiveGemsConfigManager agcm = sm.configManager.getRegisteredConfigInstance(ActiveGemsConfigManager.class);
    private final GemManager gm = sm.gemManager;

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action.equals(Action.PHYSICAL)) {
            return;
        }
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!e.getPlayer().isSneaking()) {
            return;
        }
        if (e.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }

        Player player = e.getPlayer();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (!gm.isGem(offHandItem)) {
            return;
        }

        if (!canUseGems(player)) {
            return;
        }

        e.setCancelled(true);
        handlePower(player, action, offHandItem);
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (!gm.isGem(offHandItem)) {
            return;
        }

        event.setCancelled(true);
        if (!canUseGems(player)) {
            return;
        }

        Action mappedAction = player.isSneaking() ? Action.LEFT_CLICK_AIR : Action.RIGHT_CLICK_AIR;
        handlePower(player, mappedAction, offHandItem);
    }

    private void handlePower(Player p, Action a, ItemStack item) {
        if (agcm.isGemActive(gm.getName(item))) {
            gm.runCall(item, a, p);
        } else {
            p.sendMessage(I18N.translate("GEM_DISABLED"));
        }
    }

    private boolean canUseGems(Player player) {
        if (!tdm.cantUseGems.containsKey(player)) {
            return true;
        }
        long now = System.currentTimeMillis();
        long blockedUntil = tdm.cantUseGems.get(player);
        if (now < blockedUntil) {
            player.sendMessage(I18N.translate("ON_COOLDOWN_GEMS")
                    .replace("{time}", String.valueOf((blockedUntil - now) / 1000)));
            return false;
        }
        tdm.cantUseGems.remove(player);
        return true;
    }

}
