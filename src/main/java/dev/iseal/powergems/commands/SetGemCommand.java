package dev.iseal.powergems.commands;

import dev.iseal.powergems.PowerGems;
import dev.iseal.powergems.managers.GemManager;
import dev.iseal.powergems.managers.SingletonManager;
import dev.iseal.powergems.managers.database.FluxDataManager;
import dev.iseal.sealLib.Systems.I18N.I18N;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SetGemCommand implements CommandExecutor, TabCompleter {

    private final SingletonManager sm = SingletonManager.getInstance();
    private final FluxDataManager fluxDataManager = sm.fluxDataManager;
    private final ArrayList<String> possibleTabCompletions = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(I18N.translate("NOT_PLAYER"));
            return true;
        }

        if (!player.hasPermission(command.getPermission())) {
            player.sendMessage(I18N.translate("NO_PERMISSION"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.DARK_RED + "Usage: /setgem <gem> [level] [player]");
            return true;
        }

        Player targetPlayer = player;
        if (args.length > 2) {
            targetPlayer = Bukkit.getPlayer(args[args.length - 1]);
            if (targetPlayer == null) {
                player.sendMessage(ChatColor.DARK_RED + "Player not found.");
                return true;
            }
        }

        String gemNumString = args[0];
        int gemLevel = args.length >= 2 && isNumber(args[1]) ? Integer.parseInt(args[1]) : 1;

        int gemId = -1;
        if (isNumber(gemNumString)) {
            gemId = Integer.parseInt(gemNumString);
        } else {
            gemId = GemManager.lookUpID(gemNumString);
        }

        if (gemId == -1) {
            player.sendMessage(ChatColor.DARK_RED + "Invalid gem name / ID.");
            return true;
        }

        ItemStack selectedGem = sm.gemManager.createGem(gemId, gemLevel);
        if (selectedGem == null) {
            player.sendMessage(ChatColor.DARK_RED + "Failed to create selected Flux.");
            return true;
        }

        String fluxName = sm.gemManager.getName(selectedGem);
        int fluxLevel = sm.gemManager.getLevel(selectedGem);
        Player finalTargetPlayer = targetPlayer;
        fluxDataManager.setFlux(finalTargetPlayer.getUniqueId(), fluxName, fluxLevel).thenRun(() ->
                Bukkit.getScheduler().runTask(PowerGems.getPlugin(), () -> {
                    if (!finalTargetPlayer.isOnline()) {
                        return;
                    }
                    moveOffhandItemSafely(finalTargetPlayer);
                    finalTargetPlayer.getInventory().setItemInOffHand(selectedGem);
                    finalTargetPlayer.sendMessage(ChatColor.GREEN + "Your Flux was set to " + ChatColor.AQUA + fluxName + " Flux");
                    if (!finalTargetPlayer.getUniqueId().equals(player.getUniqueId())) {
                        player.sendMessage(ChatColor.GREEN + "Set " + finalTargetPlayer.getName() + "'s Flux to " + ChatColor.AQUA + fluxName + " Flux");
                    }
                })
        ).exceptionally(ex -> {
            Bukkit.getScheduler().runTask(PowerGems.getPlugin(), () ->
                    player.sendMessage(ChatColor.DARK_RED + "Failed to set Flux in database.")
            );
            PowerGems.getPlugin().getLogger().warning("Failed to set flux for " + finalTargetPlayer.getName() + ": " + ex.getMessage());
            return null;
        });

        return true;
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

    private boolean isNumber(String num) {
        try {
            return Integer.parseInt(num) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                                      @NotNull String[] args) {
        if (possibleTabCompletions.isEmpty()) {
            IntStream.range(0, SingletonManager.TOTAL_GEM_AMOUNT)
                    .mapToObj(GemManager::lookUpName)
                    .forEach(possibleTabCompletions::add);
        }

        if (args.length == 1) {
            return possibleTabCompletions.stream()
                    .filter(str -> str.toLowerCase().contains(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return List.of("<level>");
        } else if (args.length > 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
