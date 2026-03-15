package dev.iseal.powergems.commands;

import dev.iseal.powergems.PowerGems;
import dev.iseal.powergems.listeners.FluxSelectGuiListener;
import dev.iseal.powergems.managers.SingletonManager;
import dev.iseal.powergems.managers.database.FluxDataManager;
import dev.iseal.sealLib.Systems.I18N.I18N;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class SelectFluxCommand implements CommandExecutor {

    private final FluxDataManager fluxDataManager = SingletonManager.getInstance().fluxDataManager;
    private final FluxSelectGuiListener guiListener;

    public SelectFluxCommand(FluxSelectGuiListener guiListener) {
        this.guiListener = guiListener;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(I18N.translate("NOT_PLAYER"));
            return true;
        }
        if (!player.hasPermission("powergems.fluxplus")) {
            player.sendMessage(I18N.translate("NO_PERMISSION"));
            return true;
        }

        fluxDataManager.getPlayerData(player.getUniqueId()).thenAccept(data -> {
            long remaining = fluxDataManager.getRemainingSelectCooldown(data);
            Bukkit.getScheduler().runTask(PowerGems.getPlugin(), () -> {
                if (remaining > 0L) {
                    long hours = TimeUnit.MILLISECONDS.toHours(remaining);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60;
                    player.sendMessage(ChatColor.RED + "Select Flux cooldown: " + hours + "h " + minutes + "m remaining.");
                    return;
                }
                guiListener.openFor(player);
            });
        });
        return true;
    }
}
