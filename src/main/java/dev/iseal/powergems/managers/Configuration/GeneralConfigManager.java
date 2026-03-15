package dev.iseal.powergems.managers.Configuration;

import de.leonhard.storage.Config;
import dev.iseal.powergems.PowerGems;
import dev.iseal.powergems.misc.AbstractClasses.AbstractConfigManager;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GeneralConfigManager extends AbstractConfigManager {

    public GeneralConfigManager() {
        super(null);
        file = new Config("config", PowerGems.getPlugin().getDataFolder()+"");
    }

    public void setUpConfig() {
        file.setDefault("allowCosmeticParticleEffects", true);
        file.setDefault("allowMetrics", true);
        file.setDefault("allowMovingGems", false);
        file.setDefault("allowOnlyOneGem", false);
        file.setDefault("attemptFixOldGems", true);
        file.setDefault("blockedReplacingBlocks",
                new Material[] { Material.BEDROCK, Material.WATER, Material.NETHERITE_BLOCK });
        file.setDefault("canCraftGems", true);
        file.setDefault("canDropGems", false);
        file.setDefault("canUpgradeGems", true);
        file.setDefault("cooldownBoostPerLevelInSeconds", 2L);
        file.setDefault("cosmeticParticleEffectInterval", 5L);
        file.setDefault("countryCode", "US");
        file.setDefault("debugMode", false);
        file.setDefault("delayToUseGemsOnJoin", 30);
        file.setDefault("doDebuffForTemperature", true);
        file.setDefault("doGemDecay", true);
        file.setDefault("doGemDecayOnLevel1", false);
        file.setDefault("dragonEggHalfCooldown", true);
        file.setDefault("explosionDamageAllowed", true);
        file.setDefault("gemCacheExpireTime", 5);
        file.setDefault("gemCreationAttempts", 10);
        file.setDefault("gemsHaveDescriptions", true);
        file.setDefault("giveGemOnFirstLogin", true);
        file.setDefault("unlockNewAbilitiesOnLevelX", 3);
        file.setDefault("giveGemPermanentEffectOnLevelX", true);
        file.setDefault("unlockShiftAbilityOnLevelX", false);
        file.setDefault("isWorldGuardSupportEnabled", true);
        file.setDefault("isCombatLogXSupportEnabled", true);
        file.setDefault("keepGemsOnDeath", true);
        file.setDefault("languageCode", "en");
        file.setDefault("maxGemLevel", 5);
        file.setDefault("preventGemPowerTampering", true);
        file.setDefault("randomizedColors", false);
        file.setDefault("runUpdater", true);
        file.setDefault("upgradeGemOnKill", false);
        file.setDefault("analyticsID", generateAnalyticsId());
        writeCommentedConfigFile();
    }

    @Override
    public void lateInit() {
    }

    public static String generateAnalyticsId() {
        int number = ThreadLocalRandom.current().nextInt(0, 1_000_000_000);
        return String.format("AA-%09d", number);
    }

    public boolean allowCosmeticParticleEffects() {
        return file.getBoolean("allowCosmeticParticleEffects");
    }

    public boolean isAllowMetrics() {
        return file.getBoolean("allowMetrics");
    }

    public String getAnalyticsID() {
        return file.getString("analyticsID");
    }

    public boolean isAllowMovingGems() {
        return file.getBoolean("allowMovingGems");
    }

    public boolean allowOnlyOneGem() {
        return file.getBoolean("allowOnlyOneGem");
    }

    public boolean doAttemptFixOldGems() {
        return file.getBoolean("attemptFixOldGems");
    }

    public boolean isBlockedReplacingBlock(Block block) {
        List<String> blocks = file.getStringList("blockedReplacingBlocks");
        for (String mat : blocks) {
            Material material = Material.valueOf(mat);
            if (block.getType().equals(material)) {
                return true;
            }
        }
        return false;
    }

    public boolean canCraftGems() {
        return file.getBoolean("canCraftGems");
    }

    public boolean canDropGems() {
        return file.getBoolean("canDropGems");
    }

    public boolean canUpgradeGems() {
        return file.getBoolean("canUpgradeGems");
    }

    public long getGemCooldownBoost() {
        return file.getLong("cooldownBoostPerLevelInSeconds");
    }

    public long cosmeticParticleEffectInterval() {
        return file.getLong("cosmeticParticleEffectInterval");
    }

    public String getCountryCode() {
        return file.getString("countryCode");
    }

    public boolean isDebugMode() {
        return file.getBoolean("debugMode");
    }

    public long getDelayToUseGems() {
        return file.getLong("delayToUseGemsOnJoin");
    }

    public boolean doDebuffForTemperature() {
        return file.getBoolean("doDebuffForTemperature");
    }

    public boolean doGemDecay() {
        return file.getBoolean("doGemDecay");
    }

    public boolean doGemDecayOnLevelOne() {
        return file.getBoolean("doGemDecayOnLevel1");
    }

    public boolean isDragonEggHalfCooldown() {
        return file.getBoolean("dragonEggHalfCooldown");
    }

    public boolean isExplosionDamageAllowed() {
        return file.getBoolean("explosionDamageAllowed");
    }

    public int getGemCacheExpireTime() {
        return file.getInt("gemCacheExpireTime");
    }

    public int getGemCreationAttempts() {
        return file.getInt("gemCreationAttempts");
    }

    public boolean doGemDescriptions() {
        return file.getBoolean("gemsHaveDescriptions");
    }

    public boolean getGiveGemOnFirstLogin() {
        return file.getBoolean("giveGemOnFirstLogin");
    }

    public boolean giveGemPermanentEffectOnLvlX() {
        return file.getBoolean("giveGemPermanentEffectOnLevelX");
    }

    public boolean isWorldGuardEnabled() {
        return file.getBoolean("isWorldGuardSupportEnabled");
    }

    public boolean isCombatLogXEnabled() {
        return file.getBoolean("isCombatLogXSupportEnabled");
    }

    public boolean doKeepGemsOnDeath() {
        return file.getBoolean("keepGemsOnDeath");
    }

    public String getLanguageCode() {
        return file.getString("languageCode");
    }

    public int getMaxGemLevel() {
        return file.getInt("maxGemLevel");
    }

    public boolean doGemPowerTampering() {
        return file.getBoolean("preventGemPowerTampering");
    }

    public boolean isRandomizedColors() {
        return file.getBoolean("randomizedColors");
    }

    public boolean canRunUpdater() {
        return file.getBoolean("runUpdater");
    }

    public boolean upgradeGemOnKill() {
        return file.getBoolean("upgradeGemOnKill");
    }

    public boolean unlockShiftAbilityOnLevelX() {
        return file.getBoolean("unlockShiftAbilityOnLevelX");
    }

    public int unlockNewAbilitiesOnLevelX() {
        return file.getInt("unlockNewAbilitiesOnLevelX");
    }

    private void writeCommentedConfigFile() {
        Path path = Path.of(PowerGems.getPlugin().getDataFolder().getPath(), "config.yml");
        if (Files.exists(path)) {
            try {
                try (var lines = Files.lines(path, StandardCharsets.UTF_8)) {
                    if (lines.anyMatch(line -> line.contains("# PowerGems main configuration"))) {
                        return;
                    }
                }
            } catch (IOException ignored) {
                // If we fail to read, we attempt to rewrite safely below.
            }
        }
        String lineSeparator = System.lineSeparator();
        String blocked = String.join(", ", file.getStringList("blockedReplacingBlocks"));

        String content =
                "# PowerGems main configuration" + lineSeparator +
                "# Every option below controls plugin behavior globally." + lineSeparator +
                lineSeparator +
                "# Cosmetic particles around players that hold fluxes." + lineSeparator +
                "allowCosmeticParticleEffects: " + allowCosmeticParticleEffects() + lineSeparator +
                lineSeparator +
                "# Send anonymous usage metrics and analytics events." + lineSeparator +
                "allowMetrics: " + isAllowMetrics() + lineSeparator +
                lineSeparator +
                "# Allow moving fluxes into non-player inventories/containers." + lineSeparator +
                "allowMovingGems: " + isAllowMovingGems() + lineSeparator +
                lineSeparator +
                "# Enforce one flux per player inventory." + lineSeparator +
                "allowOnlyOneGem: " + allowOnlyOneGem() + lineSeparator +
                lineSeparator +
                "# Try auto-fixing old/broken flux metadata." + lineSeparator +
                "attemptFixOldGems: " + doAttemptFixOldGems() + lineSeparator +
                lineSeparator +
                "# Blocks protected from Sand flux temporary replacement." + lineSeparator +
                "blockedReplacingBlocks: [" + blocked + "]" + lineSeparator +
                lineSeparator +
                "# Enable random flux crafting recipe." + lineSeparator +
                "canCraftGems: " + canCraftGems() + lineSeparator +
                lineSeparator +
                "# Allow dropping flux items." + lineSeparator +
                "canDropGems: " + canDropGems() + lineSeparator +
                lineSeparator +
                "# Enable flux level upgrade crafting recipes." + lineSeparator +
                "canUpgradeGems: " + canUpgradeGems() + lineSeparator +
                lineSeparator +
                "# Cooldown reduction (seconds) gained per flux level." + lineSeparator +
                "cooldownBoostPerLevelInSeconds: " + getGemCooldownBoost() + lineSeparator +
                lineSeparator +
                "# Ticks between cosmetic particle updates." + lineSeparator +
                "cosmeticParticleEffectInterval: " + cosmeticParticleEffectInterval() + lineSeparator +
                lineSeparator +
                "# Language country code for translations (ex: US, FR)." + lineSeparator +
                "countryCode: \"" + getCountryCode() + "\"" + lineSeparator +
                lineSeparator +
                "# Enable debug mode behavior." + lineSeparator +
                "debugMode: " + isDebugMode() + lineSeparator +
                lineSeparator +
                "# Delay in seconds after join before fluxes can be used." + lineSeparator +
                "delayToUseGemsOnJoin: " + getDelayToUseGems() + lineSeparator +
                lineSeparator +
                "# Apply biome temperature debuffs to opposing flux types." + lineSeparator +
                "doDebuffForTemperature: " + doDebuffForTemperature() + lineSeparator +
                lineSeparator +
                "# Decrease flux level by one when restoring after death." + lineSeparator +
                "doGemDecay: " + doGemDecay() + lineSeparator +
                lineSeparator +
                "# If true, level 1 fluxes are also removed by decay." + lineSeparator +
                "doGemDecayOnLevel1: " + doGemDecayOnLevelOne() + lineSeparator +
                lineSeparator +
                "# Dragon Egg in inventory halves active ability cooldowns." + lineSeparator +
                "dragonEggHalfCooldown: " + isDragonEggHalfCooldown() + lineSeparator +
                lineSeparator +
                "# Allow terrain/entity damage from gem explosions." + lineSeparator +
                "explosionDamageAllowed: " + isExplosionDamageAllowed() + lineSeparator +
                lineSeparator +
                "# Seconds to cache player flux inventory scans." + lineSeparator +
                "gemCacheExpireTime: " + getGemCacheExpireTime() + lineSeparator +
                lineSeparator +
                "# Max attempts when selecting a random active flux." + lineSeparator +
                "gemCreationAttempts: " + getGemCreationAttempts() + lineSeparator +
                lineSeparator +
                "# Show lore descriptions on flux items." + lineSeparator +
                "gemsHaveDescriptions: " + doGemDescriptions() + lineSeparator +
                lineSeparator +
                "# Give a random flux on a player's very first login." + lineSeparator +
                "giveGemOnFirstLogin: " + getGiveGemOnFirstLogin() + lineSeparator +
                lineSeparator +
                "# Required level to unlock gated abilities (like shift)." + lineSeparator +
                "unlockNewAbilitiesOnLevelX: " + unlockNewAbilitiesOnLevelX() + lineSeparator +
                lineSeparator +
                "# Enable passive permanent effects after unlock level." + lineSeparator +
                "giveGemPermanentEffectOnLevelX: " + giveGemPermanentEffectOnLvlX() + lineSeparator +
                lineSeparator +
                "# Gate shift ability until unlock level is reached." + lineSeparator +
                "unlockShiftAbilityOnLevelX: " + unlockShiftAbilityOnLevelX() + lineSeparator +
                lineSeparator +
                "# Enable WorldGuard integration." + lineSeparator +
                "isWorldGuardSupportEnabled: " + isWorldGuardEnabled() + lineSeparator +
                lineSeparator +
                "# Enable CombatLogX integration." + lineSeparator +
                "isCombatLogXSupportEnabled: " + isCombatLogXEnabled() + lineSeparator +
                lineSeparator +
                "# Keep fluxes through death/respawn flow." + lineSeparator +
                "keepGemsOnDeath: " + doKeepGemsOnDeath() + lineSeparator +
                lineSeparator +
                "# Language code for translations (ex: en, fr)." + lineSeparator +
                "languageCode: \"" + getLanguageCode() + "\"" + lineSeparator +
                lineSeparator +
                "# Maximum possible flux level." + lineSeparator +
                "maxGemLevel: " + getMaxGemLevel() + lineSeparator +
                lineSeparator +
                "# Prevent tampering with temporary gem-power entities/blocks." + lineSeparator +
                "preventGemPowerTampering: " + doGemPowerTampering() + lineSeparator +
                lineSeparator +
                "# Use random display colors instead of per-flux configured colors." + lineSeparator +
                "randomizedColors: " + isRandomizedColors() + lineSeparator +
                lineSeparator +
                "# Enable automatic update checker." + lineSeparator +
                "runUpdater: " + canRunUpdater() + lineSeparator +
                lineSeparator +
                "# Upgrade all owned fluxes when a player gets a kill." + lineSeparator +
                "upgradeGemOnKill: " + upgradeGemOnKill() + lineSeparator +
                lineSeparator +
                "# Anonymous analytics identifier for this server." + lineSeparator +
                "analyticsID: \"" + getAnalyticsID() + "\"" + lineSeparator;

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            PowerGems.getPlugin().getLogger().warning("Failed to write commented config.yml: " + e.getMessage());
        }
    }
}
